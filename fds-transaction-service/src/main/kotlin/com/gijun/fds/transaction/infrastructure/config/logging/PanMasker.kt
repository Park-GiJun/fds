package com.gijun.fds.transaction.infrastructure.config.logging

internal object PanMasker {
    private val PAN_REGEX = Regex("""\b\d{13,19}\b""")

    fun mask(raw: String): String = PAN_REGEX.replace(raw) { match ->
        val digits = match.value
        if (digits.length < 13) digits
        else digits.take(6) + "*".repeat(digits.length - 10) + digits.takeLast(4)
    }
}
