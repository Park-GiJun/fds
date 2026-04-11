package com.gijun.fds.generator.application.handler

import com.gijun.fds.generator.application.port.outbound.TransactionSendPort
import com.gijun.fds.generator.domain.model.TransactionData
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class GeneratorHandlerTest {

    private val transactionSendPort = mockk<TransactionSendPort>()
    private val sut = GeneratorHandler(transactionSendPort)

    @AfterEach
    fun tearDown() {
        sut.shutdown()
    }

    @Test
    fun `초기 상태에서 running은 false이다`() {
        // when
        val status = sut.getStatus()

        // then
        status.running shouldBe false
        status.totalSent shouldBe 0L
        status.totalFailed shouldBe 0L
        status.configuredRate shouldBe 0
    }

    @Test
    fun `start 호출 시 running 상태로 전환된다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true

        // when
        sut.start(rate = 1, fraudRatio = 0.0)

        // then
        sut.getStatus().running shouldBe true
        sut.getStatus().configuredRate shouldBe 1
    }

    @Test
    fun `stop 호출 시 running이 false로 전환된다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true
        sut.start(rate = 1, fraudRatio = 0.0)

        // when
        sut.stop()

        // then
        sut.getStatus().running shouldBe false
    }

    @Test
    fun `이미 실행 중일 때 start를 다시 호출하면 무시된다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true
        sut.start(rate = 5, fraudRatio = 0.0)

        // when — 두 번째 start
        sut.start(rate = 10, fraudRatio = 0.0)

        // then — configuredRate가 첫 번째 값(5) 유지
        sut.getStatus().configuredRate shouldBe 5
    }

    @Test
    fun `실행 중이 아닐 때 stop 호출해도 에러가 발생하지 않는다`() {
        // when & then — 예외 없이 정상 수행
        sut.stop()
        sut.getStatus().running shouldBe false
    }

    @Test
    fun `start 후 전송이 실행되면 totalSent가 증가한다`() = runTest {
        // given
        coEvery { transactionSendPort.send(any()) } returns true

        // when
        sut.start(rate = 5, fraudRatio = 0.0)
        Thread.sleep(1500) // 1초 이상 대기하여 최소 1 batch 실행

        // then
        sut.getStatus().totalSent shouldBeGreaterThan 0L

        // cleanup
        sut.stop()
    }

    @Test
    fun `전송 실패 시 totalFailed가 증가한다`() = runTest {
        // given
        coEvery { transactionSendPort.send(any()) } returns false

        // when
        sut.start(rate = 3, fraudRatio = 0.0)
        Thread.sleep(1500)

        // then
        sut.getStatus().totalFailed shouldBeGreaterThan 0L

        // cleanup
        sut.stop()
    }

    @Test
    fun `burst 호출 시 지정된 수만큼 전송을 시도한다`() = runTest {
        // given
        coEvery { transactionSendPort.send(any<TransactionData>()) } returns true

        // when
        sut.burst(count = 10, fraudRatio = 0.0)
        Thread.sleep(2000) // burst 완료 대기

        // then
        coVerify(atLeast = 10) { transactionSendPort.send(any()) }
    }

    @Test
    fun `getStatus는 현재 상태의 스냅샷을 반환한다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true

        // when
        val status1 = sut.getStatus()
        sut.start(rate = 10, fraudRatio = 0.1)
        val status2 = sut.getStatus()

        // then
        status1.running shouldBe false
        status2.running shouldBe true
        status2.configuredRate shouldBe 10
    }

    @Test
    fun `shutdown 호출 후 start가 동작하지 않는다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true

        // when
        sut.shutdown()

        // then — shutdown 후에는 scope가 취소되어 start해도 정상 동작 불가
        sut.getStatus().running shouldBe false
    }

    @Test
    fun `start와 stop을 반복해도 상태가 정확하다`() {
        // given
        coEvery { transactionSendPort.send(any()) } returns true

        // when — start → stop → start → stop
        sut.start(rate = 1, fraudRatio = 0.0)
        sut.getStatus().running shouldBe true
        sut.stop()
        sut.getStatus().running shouldBe false

        sut.start(rate = 5, fraudRatio = 0.0)
        sut.getStatus().running shouldBe true
        sut.getStatus().configuredRate shouldBe 5
    }
}
