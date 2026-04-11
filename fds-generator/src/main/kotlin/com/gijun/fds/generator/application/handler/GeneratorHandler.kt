package com.gijun.fds.generator.application.handler

import com.gijun.fds.generator.application.port.`in`.GeneratorUseCase
import com.gijun.fds.generator.application.port.out.TransactionSendPort
import com.gijun.fds.generator.domain.model.FraudType
import com.gijun.fds.generator.domain.model.GeneratorStatus
import com.gijun.fds.generator.domain.model.TransactionDataFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

@Service
class GeneratorHandler(
    private val transactionSendPort: TransactionSendPort,
) : GeneratorUseCase {

    private val log = LoggerFactory.getLogger(javaClass)
    private val running = AtomicBoolean(false)
    private val totalSent = AtomicLong(0)
    private val totalFailed = AtomicLong(0)
    private var currentRate = 0
    private var job: Job? = null

    override fun start(rate: Int, fraudRatio: Double) {
        if (running.getAndSet(true)) {
            log.warn("Generator is already running")
            return
        }

        log.info("Starting generator: rate={}/s, fraudRatio={}", rate, fraudRatio)
        totalSent.set(0)
        totalFailed.set(0)
        currentRate = rate

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && running.get()) {
                val batchStart = System.currentTimeMillis()

                coroutineScope {
                    repeat(rate) {
                        launch {
                            val tx = generateTransaction(fraudRatio)
                            if (transactionSendPort.send(tx)) {
                                totalSent.incrementAndGet()
                            } else {
                                totalFailed.incrementAndGet()
                            }
                        }
                    }
                }

                val elapsed = System.currentTimeMillis() - batchStart
                val sleepTime = 1000L - elapsed
                if (sleepTime > 0) delay(sleepTime)

                if (totalSent.get() % (rate * 10) == 0L) {
                    log.info("Generator stats: sent={}, failed={}", totalSent.get(), totalFailed.get())
                }
            }
        }
    }

    override fun stop() {
        if (!running.getAndSet(false)) {
            log.warn("Generator is not running")
            return
        }
        job?.cancel()
        log.info("Generator stopped. Total sent={}, failed={}", totalSent.get(), totalFailed.get())
    }

    override fun burst(count: Int, fraudRatio: Double) {
        log.info("Burst mode: sending {} transactions (fraudRatio={})", count, fraudRatio)

        CoroutineScope(Dispatchers.IO).launch {
            coroutineScope {
                repeat(count) {
                    launch {
                        val tx = generateTransaction(fraudRatio)
                        transactionSendPort.send(tx)
                    }
                }
            }
            log.info("Burst completed: {} transactions sent", count)
        }
    }

    override fun getStatus(): GeneratorStatus = GeneratorStatus(
        running = running.get(),
        totalSent = totalSent.get(),
        totalFailed = totalFailed.get(),
        configuredRate = currentRate,
    )

    private fun generateTransaction(fraudRatio: Double) =
        if (Random.nextDouble() < fraudRatio) {
            TransactionDataFactory.createSuspicious(FraudType.entries.random())
        } else {
            TransactionDataFactory.createNormal()
        }
}