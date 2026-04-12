package com.gijun.fds.transaction.infrastructure.config.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class CardNumberMaskingConverterTest {
    private val converter = CardNumberMaskingConverter()

    private fun event(msg: String): ILoggingEvent = mockk<ILoggingEvent>().also {
        every { it.formattedMessage } returns msg
        every { it.throwableProxy } returns null
    }

    @Test
    fun `16자리 PAN 마스킹 - 앞6 뒤4 노출`() {
        converter.convert(event("paid with 4111111111111111 ok")) shouldBe "paid with 411111******1111 ok"
    }

    @Test
    fun `13자리 최소 PAN 마스킹`() {
        converter.convert(event("card 4111111111111 end")) shouldBe "card 411111***1111 end"
    }

    @Test
    fun `19자리 최대 PAN 마스킹`() {
        converter.convert(event("long 4111111111111111111 x")) shouldBe "long 411111*********1111 x"
    }

    @Test
    fun `12자리 미만 숫자는 마스킹하지 않음`() {
        converter.convert(event("pin 123456789012")) shouldBe "pin 123456789012"
    }

    @Test
    fun `20자리 초과 숫자는 마스킹하지 않음`() {
        converter.convert(event("num 41111111111111111111")) shouldBe "num 41111111111111111111"
    }

    @Test
    fun `빈 메시지`() {
        converter.convert(event("")) shouldBe ""
    }

    @Test
    fun `여러 PAN 동시 마스킹`() {
        converter.convert(event("a 4111111111111111 b 5555555555554444 c")) shouldBe "a 411111******1111 b 555555******4444 c"
    }
}
