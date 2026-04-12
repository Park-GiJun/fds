package com.gijun.fds.transaction.domain.vo

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CountryCodeTest {
    @Test
    fun `대문자 3자리 - OK`() {
        CountryCode("USA").value shouldBe "USA"
    }

    @Test
    fun `소문자 - 거부`() {
        assertThrows<IllegalArgumentException> { CountryCode("usa") }
    }

    @Test
    fun `2자리 - 거부`() {
        assertThrows<IllegalArgumentException> { CountryCode("US") }
    }

    @Test
    fun `4자리 - 거부`() {
        assertThrows<IllegalArgumentException> { CountryCode("USAA") }
    }

    @Test
    fun `빈 문자열 - 거부`() {
        assertThrows<IllegalArgumentException> { CountryCode("") }
    }

    @Test
    fun `숫자 포함 - 거부`() {
        assertThrows<IllegalArgumentException> { CountryCode("US1") }
    }
}
