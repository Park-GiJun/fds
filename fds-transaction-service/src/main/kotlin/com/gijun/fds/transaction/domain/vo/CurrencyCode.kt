package com.gijun.fds.transaction.domain.vo

@JvmInline
value class CurrencyCode(val value: String) {
    init {
        require(ISO_4217_PATTERN.matches(value)) { "currency must be ISO 4217 uppercase, got: $value" }
    }
    override fun toString(): String = value

    companion object {
        private val ISO_4217_PATTERN = Regex("^[A-Z]{3}$")
    }
}
