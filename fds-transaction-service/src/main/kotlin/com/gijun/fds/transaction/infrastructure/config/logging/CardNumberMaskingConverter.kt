package com.gijun.fds.transaction.infrastructure.config.logging

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class CardNumberMaskingConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        val raw = event.formattedMessage ?: return ""
        return PAN_REGEX.replace(raw) { match ->
            val digits = match.value
            if (digits.length < 13) digits
            else digits.take(6) + "*".repeat(digits.length - 10) + digits.takeLast(4)
        }
    }

    companion object {
        private val PAN_REGEX = Regex("""\b\d{13,19}\b""")
    }
}
