package com.gijun.fds.common.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class CardMaskingTest {

    @Test
    fun `16자리 카드번호 마스킹 시 뒤 4자리만 노출된다`() {
        // given
        val cardNumber = "4123456789012345"

        // when
        val masked = CardMasking.mask(cardNumber)

        // then
        masked shouldEndWith "2345"
        masked.length shouldBe 16
    }

    @Test
    fun `마스킹 결과에 원본 앞자리가 포함되지 않는다`() {
        // given
        val cardNumber = "4111222233334444"

        // when
        val masked = CardMasking.mask(cardNumber)

        // then
        masked shouldEndWith "4444"
        masked shouldNotContain "41112222"
    }

    @Test
    fun `8자리 미만 카드번호는 전체 마스킹된다`() {
        // when
        val masked = CardMasking.mask("1234567")

        // then
        masked shouldBe "****"
    }

    @Test
    fun `빈 문자열은 전체 마스킹된다`() {
        // when
        val masked = CardMasking.mask("")

        // then
        masked shouldBe "****"
    }

    @Test
    fun `정확히 8자리 카드번호는 마스킹된다`() {
        // when
        val masked = CardMasking.mask("12345678")

        // then
        masked shouldEndWith "5678"
        masked.length shouldBe 8
    }
}
