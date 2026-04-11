package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionTest {

    private val now = Instant.parse("2026-01-01T00:00:00Z")
    private val later = Instant.parse("2026-01-01T01:00:00Z")

    private fun createTransaction(
        status: TransactionStatus = TransactionStatus.PENDING,
    ) = Transaction(
        transactionId = "tx-001",
        userId = "USER_00001",
        cardNumber = "4111222233334444",
        maskedCardNumber = "411122******4444",
        amount = BigDecimal(50000),
        currency = "KRW",
        merchantName = "스타벅스",
        merchantCategory = "CAFE",
        country = "KR",
        city = "서울",
        latitude = 37.5665,
        longitude = 126.9780,
        status = status,
        createdAt = now,
        updatedAt = now,
    )

    // === applyDetectionResult ===

    @Test
    fun `LOW riskLevel 적용 시 APPROVED 상태가 된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.LOW, riskScore = 20, now = later)

        result.status shouldBe TransactionStatus.APPROVED
        result.riskLevel shouldBe RiskLevel.LOW
        result.riskScore shouldBe 20
        result.updatedAt shouldBe later
    }

    @Test
    fun `MEDIUM riskLevel 적용 시 APPROVED 상태가 된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.MEDIUM, riskScore = 50, now = later)

        result.status shouldBe TransactionStatus.APPROVED
    }

    @Test
    fun `HIGH riskLevel 적용 시 BLOCKED 상태가 된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.HIGH, riskScore = 80, now = later)

        result.status shouldBe TransactionStatus.BLOCKED
        result.riskLevel shouldBe RiskLevel.HIGH
    }

    @Test
    fun `CRITICAL riskLevel 적용 시 BLOCKED 상태가 된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.CRITICAL, riskScore = 95, now = later)

        result.status shouldBe TransactionStatus.BLOCKED
    }

    @Test
    fun `riskScore 0은 허용된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.LOW, riskScore = 0, now = later)

        result.riskScore shouldBe 0
    }

    @Test
    fun `riskScore 100은 허용된다`() {
        val tx = createTransaction()

        val result = tx.applyDetectionResult(RiskLevel.HIGH, riskScore = 100, now = later)

        result.riskScore shouldBe 100
    }

    @Test
    fun `riskScore 음수면 IllegalArgumentException이 발생한다`() {
        val tx = createTransaction()

        shouldThrow<IllegalArgumentException> {
            tx.applyDetectionResult(RiskLevel.LOW, riskScore = -1, now = later)
        }
    }

    @Test
    fun `riskScore 101이면 IllegalArgumentException이 발생한다`() {
        val tx = createTransaction()

        shouldThrow<IllegalArgumentException> {
            tx.applyDetectionResult(RiskLevel.LOW, riskScore = 101, now = later)
        }
    }

    @Test
    fun `applyDetectionResult는 원본을 변경하지 않는다`() {
        val tx = createTransaction()

        tx.applyDetectionResult(RiskLevel.HIGH, riskScore = 80, now = later)

        tx.status shouldBe TransactionStatus.PENDING
        tx.riskLevel shouldBe null
    }

    // === markSuspicious ===

    @Test
    fun `PENDING 상태에서 SUSPICIOUS로 전환된다`() {
        val tx = createTransaction(status = TransactionStatus.PENDING)

        val result = tx.markSuspicious(now = later)

        result.status shouldBe TransactionStatus.SUSPICIOUS
        result.updatedAt shouldBe later
    }

    @Test
    fun `APPROVED 상태에서 markSuspicious 호출 시 예외가 발생한다`() {
        val tx = createTransaction(status = TransactionStatus.APPROVED)

        shouldThrow<IllegalArgumentException> {
            tx.markSuspicious(now = later)
        }
    }

    @Test
    fun `BLOCKED 상태에서 markSuspicious 호출 시 예외가 발생한다`() {
        val tx = createTransaction(status = TransactionStatus.BLOCKED)

        shouldThrow<IllegalArgumentException> {
            tx.markSuspicious(now = later)
        }
    }

    @Test
    fun `markSuspicious는 원본을 변경하지 않는다`() {
        val tx = createTransaction()

        tx.markSuspicious(now = later)

        tx.status shouldBe TransactionStatus.PENDING
    }
}
