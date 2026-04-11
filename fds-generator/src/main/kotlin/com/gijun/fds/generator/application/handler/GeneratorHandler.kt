package com.gijun.fds.generator.application.handler

import com.gijun.fds.generator.application.port.inbound.GeneratorUseCase
import com.gijun.fds.generator.application.port.outbound.TransactionSendPort
import com.gijun.fds.generator.domain.model.FraudType
import com.gijun.fds.generator.domain.model.GeneratorStatus
import com.gijun.fds.generator.domain.model.TransactionDataFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

class GeneratorHandler(
    private val transactionSendPort: TransactionSendPort,
) : GeneratorUseCase {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    private val totalSent = AtomicLong(0)
    private val totalFailed = AtomicLong(0)
    private val currentRate = AtomicInteger(0)
    private var job: Job? = null

    override fun start(rate: Int, fraudRatio: Double) {
        require(rate > 0) { "rate must be positive, got $rate" }
        require(fraudRatio in 0.0..1.0) { "fraudRatio must be in [0.0, 1.0], got $fraudRatio" }

        if (running.getAndSet(true)) {
            log.warn("Generator is already running")
            return
        }

        log.info("Starting generator: rate={}/s, fraudRatio={}", rate, fraudRatio)
        totalSent.set(0)
        totalFailed.set(0)
        currentRate.set(rate)

        job = scope.launch {
            var lastLoggedCount = 0L
            val logInterval = rate.toLong() * 10
            val semaphore = Semaphore(MAX_CONCURRENT_SEND)

            while (isActive && running.get()) {
                val batchStart = System.currentTimeMillis()

                coroutineScope {
                    repeat(rate) {
                        launch {
                            semaphore.acquire()
                            try {
                                val tx = generateTransaction(fraudRatio)
                                if (transactionSendPort.send(tx)) {
                                    totalSent.incrementAndGet()
                                } else {
                                    totalFailed.incrementAndGet()
                                }
                            } finally {
                                semaphore.release()
                            }
                        }
                    }
                }

                val sent = totalSent.get()
                if (logInterval > 0 && sent / logInterval > lastLoggedCount / logInterval) {
                    lastLoggedCount = sent
                    log.info("Generator stats: sent={}, failed={}", sent, totalFailed.get())
                }

                val elapsed = System.currentTimeMillis() - batchStart
                val sleepTime = 1000L - elapsed
                if (sleepTime > 0) delay(sleepTime)
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
        require(count > 0) { "count must be positive, got $count" }
        require(fraudRatio in 0.0..1.0) { "fraudRatio must be in [0.0, 1.0], got $fraudRatio" }
        log.info("Burst mode: sending {} transactions (fraudRatio={})", count, fraudRatio)

        scope.launch {
            val semaphore = Semaphore(MAX_CONCURRENT_BURST)
            coroutineScope {
                repeat(count) {
                    launch {
                        semaphore.acquire()
                        try {
                            val tx = generateTransaction(fraudRatio)
                            if (transactionSendPort.send(tx)) {
                                totalSent.incrementAndGet()
                            } else {
                                totalFailed.incrementAndGet()
                            }
                        } finally {
                            semaphore.release()
                        }
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
        configuredRate = currentRate.get(),
    )

    fun shutdown() {
        running.set(false)
        job?.cancel()
        scope.cancel()
    }

    private fun generateTransaction(fraudRatio: Double) =
        if (Random.nextDouble() < fraudRatio) {
            TransactionDataFactory.createSuspicious(FraudType.entries.random())
        } else {
            TransactionDataFactory.createNormal()
        }

    companion object {
        private const val MAX_CONCURRENT_SEND = 200
        private const val MAX_CONCURRENT_BURST = 200
    }
}
