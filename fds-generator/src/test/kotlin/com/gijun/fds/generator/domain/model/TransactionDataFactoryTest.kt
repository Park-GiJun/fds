package com.gijun.fds.generator.domain.model

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TransactionDataFactoryTest {

    @Test
    fun `정상 거래 생성 시 필수 필드가 모두 채워진다`() {
        // when
        val tx = TransactionDataFactory.createNormal()

        // then
        tx.transactionId.shouldNotBeBlank()
        tx.userId.shouldNotBeBlank()
        tx.cardNumber.shouldNotBeBlank()
        tx.merchantName.shouldNotBeBlank()
        tx.merchantCategory.shouldNotBeBlank()
        tx.country.shouldNotBeBlank()
        tx.city.shouldNotBeBlank()
        tx.currency.shouldNotBeBlank()
        tx.timestamp shouldNotBe null
    }

    @Test
    fun `정상 거래의 userId는 USER_ 접두사를 가진다`() {
        // when
        val tx = TransactionDataFactory.createNormal()

        // then
        tx.userId shouldStartWith "USER_"
    }

    @Test
    fun `정상 거래의 금액은 양수이다`() {
        // when — 여러 번 생성하여 확인
        repeat(20) {
            val tx = TransactionDataFactory.createNormal()
            tx.amount shouldBeGreaterThanOrEqualTo BigDecimal.ONE
        }
    }

    @Test
    fun `정상 거래의 카드번호는 4로 시작한다`() {
        // when
        val tx = TransactionDataFactory.createNormal()

        // then
        tx.cardNumber shouldStartWith "4"
    }

    @Test
    fun `정상 거래의 통화는 KRW 또는 USD이다`() {
        // when — 여러 번 생성하여 다양한 통화 확인
        val currencies = (1..50).map { TransactionDataFactory.createNormal().currency }.toSet()

        // then
        currencies.forEach { it shouldBe if (it == "KRW") "KRW" else "USD" }
    }

    @Test
    fun `정상 거래의 위도는 유효 범위 내이다`() {
        // when
        repeat(10) {
            val tx = TransactionDataFactory.createNormal()
            tx.latitude.shouldBeBetween(-90.0, 90.0, 0.0)
        }
    }

    @Test
    fun `정상 거래의 경도는 유효 범위 내이다`() {
        // when
        repeat(10) {
            val tx = TransactionDataFactory.createNormal()
            tx.longitude.shouldBeBetween(-180.0, 180.0, 0.0)
        }
    }

    @Test
    fun `의심 거래(HIGH_AMOUNT) 생성 시 금액이 3백만원 이상이다`() {
        // when
        repeat(10) {
            val tx = TransactionDataFactory.createSuspicious(FraudType.HIGH_AMOUNT)

            // then
            tx.amount shouldBeGreaterThanOrEqualTo BigDecimal(3_000_000)
        }
    }

    @Test
    fun `의심 거래(FOREIGN_AFTER_DOMESTIC) 생성 시 해외 가맹점이다`() {
        // when
        repeat(10) {
            val tx = TransactionDataFactory.createSuspicious(FraudType.FOREIGN_AFTER_DOMESTIC)

            // then
            tx.country shouldNotBe "KR"
            listOf("US", "JP", "FR", "GB") shouldContain tx.country
        }
    }

    @Test
    fun `의심 거래(MIDNIGHT) 생성 시 금액이 50만원 이상이다`() {
        // when
        repeat(10) {
            val tx = TransactionDataFactory.createSuspicious(FraudType.MIDNIGHT)

            // then
            tx.amount shouldBeGreaterThanOrEqualTo BigDecimal(500_000)
        }
    }

    @Test
    fun `매 생성마다 고유한 transactionId가 부여된다`() {
        // when
        val ids = (1..100).map { TransactionDataFactory.createNormal().transactionId }.toSet()

        // then
        ids.size shouldBe 100
    }

    @Test
    fun `모든 FraudType에 대해 의심 거래를 생성할 수 있다`() {
        // when & then
        FraudType.entries.forEach { type ->
            val tx = TransactionDataFactory.createSuspicious(type)
            tx.transactionId.shouldNotBeBlank()
        }
    }
}
