package com.gijun.fds.transaction.application.dto.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class RegisterTransactionCommandTest {
    private fun valid(override: (RegisterTransactionCommand.() -> RegisterTransactionCommand)? = null): RegisterTransactionCommand {
        val base = RegisterTransactionCommand(
            transactionId = "TX-000001",
            userId = "U-1",
            cardNumber = "4111111111111111",
            amount = BigDecimal("100.00"),
            currency = "USD",
            country = "USA",
            merchantName = "M",
            merchantCategory = "C",
            city = "Seattle",
            latitude = 0.0,
            longitude = 0.0,
        )
        return override?.invoke(base) ?: base
    }

    @Test
    fun `정상 값 - OK`() {
        valid()
    }

    @Test
    fun `transactionId 7자 미만 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(transactionId = "TX-001") }
    }

    @Test
    fun `cardNumber 문자 포함 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(cardNumber = "41111111111a") }
    }

    @Test
    fun `amount 0 이하 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(amount = BigDecimal.ZERO) }
    }

    @Test
    fun `currency 소문자 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(currency = "usd") }
    }

    @Test
    fun `latitude 범위 초과 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(latitude = 91.0) }
    }

    @Test
    fun `longitude 범위 초과 - 거부`() {
        assertThrows<IllegalArgumentException> { valid().copy(longitude = 181.0) }
    }
}
