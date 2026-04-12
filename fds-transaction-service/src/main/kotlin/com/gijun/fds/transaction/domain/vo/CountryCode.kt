package com.gijun.fds.transaction.domain.vo

@JvmInline
value class CountryCode(val value: String) {
    init {
        require(ISO_3166_PATTERN.matches(value)) { "country must be ISO 3166-1 alpha-3 uppercase, got: $value" }
    }
    override fun toString(): String = value

    companion object {
        private val ISO_3166_PATTERN = Regex("^[A-Z]{3}$")
    }
}
