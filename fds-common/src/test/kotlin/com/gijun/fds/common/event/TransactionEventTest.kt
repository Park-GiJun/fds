package com.gijun.fds.common.event

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionEventTest {

    @Test
    fun `fromRaw 호출 시 카드번호가 마스킹된다`() {
        // when
        val event = TransactionEvent.fromRaw(
            transactionId = "tx-001",
            userId = "USER_00001",
            cardNumber = "4111222233334444",
            amount = BigDecimal(50000),
            currency = "KRW",
            merchantName = "스타벅스",
            merchantCategory = "CAFE",
            country = "KR",
            city = "서울",
            latitude = 37.5665,
            longitude = 126.9780,
            timestamp = Instant.parse("2026-01-01T00:00:00Z"),
        )

        // then — 뒤 4자리만 노출, 원본 중간 자릿수 미포함
        event.maskedCardNumber shouldContain "4444"
        event.maskedCardNumber shouldNotContain "22223333"
    }

    @Test
    fun `fromRaw 필드 매핑이 정확하다`() {
        // when
        val event = TransactionEvent.fromRaw(
            transactionId = "tx-002",
            userId = "USER_00002",
            cardNumber = "4999888877776666",
            amount = BigDecimal(100000),
            currency = "USD",
            merchantName = "Amazon",
            merchantCategory = "ONLINE",
            country = "US",
            city = "Seattle",
            latitude = 47.6062,
            longitude = -122.3321,
            timestamp = Instant.parse("2026-06-15T12:00:00Z"),
        )

        // then
        event.transactionId shouldBe "tx-002"
        event.userId shouldBe "USER_00002"
        event.amount shouldBe BigDecimal(100000)
        event.currency shouldBe "USD"
        event.merchantName shouldBe "Amazon"
        event.country shouldBe "US"
    }

    @Test
    fun `maskedCardNumber 필드에 원본 카드번호가 저장되지 않는다`() {
        // given
        val rawCard = "4111222233334444"

        // when
        val event = TransactionEvent.fromRaw(
            transactionId = "tx-003",
            userId = "USER_00003",
            cardNumber = rawCard,
            amount = BigDecimal(1000),
            currency = "KRW",
            merchantName = "test",
            merchantCategory = "TEST",
            country = "KR",
            city = "서울",
            latitude = 0.0,
            longitude = 0.0,
            timestamp = Instant.now(),
        )

        // then
        event.maskedCardNumber shouldNotContain "22223333"
        (event.maskedCardNumber == rawCard) shouldBe false
    }
}
