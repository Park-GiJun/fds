package com.gijun.fds.common.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test

class CardMaskingTest {

    @Test
    fun `16자리 카드번호 마스킹 시 앞6자리와 뒤4자리만 노출된다`() {
        // given
        val cardNumber = "4123456789012345"

        // when
        val masked = CardMasking.mask(cardNumber)

        // then
        masked shouldBe "412345******2345"
        masked shouldStartWith "412345"
        masked shouldEndWith "2345"
        masked.length shouldBe 16
    }

    @Test
    fun `마스킹 결과에 중간 자릿수 원본이 포함되지 않는다`() {
        // given
        val cardNumber = "4111222233334444"

        // when
        val masked = CardMasking.mask(cardNumber)

        // then
        masked shouldStartWith "411122"
        masked shouldEndWith "4444"
        masked shouldNotContain "33334"
    }

    @Test
    fun `13자리 미만 카드번호는 전체 마스킹된다`() {
        // when
        val masked = CardMasking.mask("1234567")

        // then
        masked shouldBe "******"
    }

    @Test
    fun `빈 문자열은 전체 마스킹된다`() {
        // when
        val masked = CardMasking.mask("")

        // then
        masked shouldBe "******"
    }

    @Test
    fun `정확히 13자리 카드번호는 앞6뒤4 마스킹된다`() {
        // when
        val masked = CardMasking.mask("1234567890123")

        // then
        masked shouldStartWith "123456"
        masked shouldEndWith "0123"
        masked.length shouldBe 13
    }
}
