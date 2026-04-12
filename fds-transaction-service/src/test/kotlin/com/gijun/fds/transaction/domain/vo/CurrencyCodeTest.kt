package com.gijun.fds.transaction.domain.vo

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CurrencyCodeTest {
    @Test
    fun `대문자 3자리 - OK`() {
        CurrencyCode("USD").value shouldBe "USD"
    }

    @Test
    fun `소문자 - 거부`() {
        assertThrows<IllegalArgumentException> { CurrencyCode("usd") }
    }

    @Test
    fun `2자리 - 거부`() {
        assertThrows<IllegalArgumentException> { CurrencyCode("US") }
    }

    @Test
    fun `4자리 - 거부`() {
        assertThrows<IllegalArgumentException> { CurrencyCode("USDD") }
    }

    @Test
    fun `빈 문자열 - 거부`() {
        assertThrows<IllegalArgumentException> { CurrencyCode("") }
    }

    @Test
    fun `숫자 포함 - 거부`() {
        assertThrows<IllegalArgumentException> { CurrencyCode("US1") }
    }
}
