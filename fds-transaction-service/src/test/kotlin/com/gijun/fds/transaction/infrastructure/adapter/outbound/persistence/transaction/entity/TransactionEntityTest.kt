package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.model.Transaction
import com.gijun.fds.transaction.domain.vo.CountryCode
import com.gijun.fds.transaction.domain.vo.CurrencyCode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionEntityTest {

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    private fun createDomain() = Transaction(
        transactionId = "tx-001",
        userId = "USER_00001",
        maskedCardNumber = "411122******4444",
        amount = BigDecimal(50000),
        currency = CurrencyCode("KRW"),
        merchantName = "스타벅스",
        merchantCategory = "CAFE",
        country = CountryCode("KOR"),
        city = "서울",
        latitude = 37.5665,
        longitude = 126.9780,
        createdAt = now,
        updatedAt = now,
    )

    @Test
    fun `fromDomain으로 생성된 Entity의 필드가 정확하다`() {
        val domain = createDomain()
        val entity = TransactionEntity.fromDomain(domain, encryptedCardNumber = "ENCRYPTED_VALUE")

        entity.transactionId shouldBe "tx-001"
        entity.userId shouldBe "USER_00001"
        entity.encryptedCardNumber shouldBe "ENCRYPTED_VALUE"
        entity.maskedCardNumber shouldBe "411122******4444"
        entity.amount shouldBe BigDecimal(50000)
        entity.status shouldBe TransactionStatus.PENDING
    }

    @Test
    fun `toDomain 변환 시 maskedCardNumber가 복원된다 — 도메인은 원문을 보유하지 않는다`() {
        val entity = TransactionEntity.fromDomain(createDomain(), "ENCRYPTED")
        val domain = entity.toDomain()

        domain.maskedCardNumber shouldBe "411122******4444"
        domain.maskedCardNumber shouldNotBe "ENCRYPTED"
    }

    @Test
    fun `toDomain 변환 시 모든 필드가 매핑된다`() {
        val entity = TransactionEntity.fromDomain(createDomain(), "ENC")
        val domain = entity.toDomain()

        domain.transactionId shouldBe "tx-001"
        domain.userId shouldBe "USER_00001"
        domain.amount shouldBe BigDecimal(50000)
        domain.currency shouldBe CurrencyCode("KRW")
        domain.merchantName shouldBe "스타벅스"
        domain.country shouldBe CountryCode("KOR")
        domain.status shouldBe TransactionStatus.PENDING
        domain.riskLevel shouldBe null
    }

    @Test
    fun `updateFromDomain으로 상태 업데이트가 반영된다`() {
        val entity = TransactionEntity.fromDomain(createDomain(), "ENC")
        val updatedDomain = createDomain().applyDetectionResult(
            riskLevel = RiskLevel.HIGH,
            riskScore = 80,
            now = Instant.parse("2026-01-01T01:00:00Z"),
        )

        entity.updateFromDomain(updatedDomain)

        entity.status shouldBe TransactionStatus.BLOCKED
        entity.riskLevel shouldBe RiskLevel.HIGH
        entity.riskScore shouldBe 80
    }
}
