package com.gijun.fds.transaction.infrastructure.config.logging

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy

class CardNumberMaskingConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        val msg = mask(event.formattedMessage ?: "")
        val throwable = event.throwableProxy?.let { formatThrowable(it) } ?: ""
        return if (throwable.isEmpty()) msg else "$msg\n$throwable"
    }

    private fun formatThrowable(proxy: IThrowableProxy): String {
        val sb = StringBuilder()
        sb.append(proxy.className).append(": ").append(mask(proxy.message ?: ""))
        proxy.stackTraceElementProxyArray?.forEach { sb.append("\n\tat ").append(it.stackTraceElement) }
        proxy.cause?.let { sb.append("\nCaused by: ").append(formatThrowable(it)) }
        return sb.toString()
    }

    private fun mask(raw: String): String = PAN_REGEX.replace(raw) { match ->
        val d = match.value
        if (d.length < 13) d else d.take(6) + "*".repeat(d.length - 10) + d.takeLast(4)
    }

    companion object {
        private val PAN_REGEX = Regex("""\b\d{13,19}\b""")
    }
}
