package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.vo.CountryCode
import com.gijun.fds.transaction.domain.vo.CurrencyCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
        maskedCardNumber = "******0001",
        encryptedCardNumber = "enc-fixture",
        amount = BigDecimal(50000),
        currency = CurrencyCode("KRW"),
        merchantName = "스타벅스",
        merchantCategory = "CAFE",
        country = CountryCode("KOR"),
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
        val result = createTransaction().applyDetectionResult(RiskLevel.LOW, riskScore = 20, now = later)

        result.status shouldBe TransactionStatus.APPROVED
        result.riskLevel shouldBe RiskLevel.LOW
        result.riskScore shouldBe 20
        result.updatedAt shouldBe later
    }

    @Test
    fun `MEDIUM riskLevel 적용 시 APPROVED 상태가 된다`() {
        val result = createTransaction().applyDetectionResult(RiskLevel.MEDIUM, riskScore = 50, now = later)
        result.status shouldBe TransactionStatus.APPROVED
    }

    @Test
    fun `HIGH riskLevel 적용 시 BLOCKED 상태가 된다`() {
        val result = createTransaction().applyDetectionResult(RiskLevel.HIGH, riskScore = 80, now = later)
        result.status shouldBe TransactionStatus.BLOCKED
        result.riskLevel shouldBe RiskLevel.HIGH
    }

    @Test
    fun `CRITICAL riskLevel 적용 시 BLOCKED 상태가 된다`() {
        val result = createTransaction().applyDetectionResult(RiskLevel.CRITICAL, riskScore = 95, now = later)
        result.status shouldBe TransactionStatus.BLOCKED
    }

    @Test
    fun `riskScore 0은 허용된다`() {
        val result = createTransaction().applyDetectionResult(RiskLevel.LOW, riskScore = 0, now = later)
        result.riskScore shouldBe 0
    }

    @Test
    fun `riskScore 100은 허용된다`() {
        val result = createTransaction().applyDetectionResult(RiskLevel.HIGH, riskScore = 100, now = later)
        result.riskScore shouldBe 100
    }

    @Test
    fun `riskScore 음수면 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction().applyDetectionResult(RiskLevel.LOW, riskScore = -1, now = later)
        }
    }

    @Test
    fun `riskScore 101이면 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction().applyDetectionResult(RiskLevel.LOW, riskScore = 101, now = later)
        }
    }

    @Test
    fun `PENDING이 아닌 상태에서 applyDetectionResult 호출 시 예외가 발생한다`() {
        val tx = createTransaction(status = TransactionStatus.APPROVED)
        shouldThrow<IllegalArgumentException> {
            tx.applyDetectionResult(RiskLevel.HIGH, riskScore = 80, now = later)
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
        val result = createTransaction().markSuspicious(now = later)
        result.status shouldBe TransactionStatus.SUSPICIOUS
        result.updatedAt shouldBe later
    }

    @Test
    fun `APPROVED 상태에서 markSuspicious 호출 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction(status = TransactionStatus.APPROVED).markSuspicious(now = later)
        }
    }

    @Test
    fun `SUSPICIOUS 상태에서 markSuspicious 재호출 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction(status = TransactionStatus.SUSPICIOUS).markSuspicious(now = later)
        }
    }

    @Test
    fun `BLOCKED 상태에서 markSuspicious 호출 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction(status = TransactionStatus.BLOCKED).markSuspicious(now = later)
        }
    }

    // === approveAfterReview / blockAfterReview ===

    @Test
    fun `SUSPICIOUS 상태에서 수동 승인 시 APPROVED가 된다`() {
        val tx = createTransaction(status = TransactionStatus.SUSPICIOUS)
        val result = tx.approveAfterReview(now = later)
        result.status shouldBe TransactionStatus.APPROVED
        result.updatedAt shouldBe later
    }

    @Test
    fun `SUSPICIOUS 상태에서 수동 차단 시 BLOCKED가 된다`() {
        val tx = createTransaction(status = TransactionStatus.SUSPICIOUS)
        val result = tx.blockAfterReview(now = later)
        result.status shouldBe TransactionStatus.BLOCKED
    }

    @Test
    fun `PENDING 상태에서 approveAfterReview 호출 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction().approveAfterReview(now = later)
        }
    }

    @Test
    fun `PENDING 상태에서 blockAfterReview 호출 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            createTransaction().blockAfterReview(now = later)
        }
    }

    // === factory ===

    @Test
    fun `create 팩토리로 생성 시 maskedCardNumber가 자동 생성된다`() {
        val tx = Transaction.create(
            transactionId = "tx-002", userId = "USER_00002",
            plainCardNumber = "4111222233334444",
            encryptedCardNumber = "enc-fixture",
            amount = BigDecimal(10000), currency = "KRW",
            merchantName = "테스트", merchantCategory = "TEST",
            country = "KOR", city = "서울",
            latitude = 37.0, longitude = 127.0, now = now,
        )
        tx.maskedCardNumber shouldContain "4444"
        tx.maskedCardNumber shouldContain "411122"
        tx.status shouldBe TransactionStatus.PENDING
    }
}
