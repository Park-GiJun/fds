package com.gijun.fds.common.domain

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    ;

    fun isBlockLevel(): Boolean = this in BLOCK_LEVELS

    companion object {
        private val BLOCK_LEVELS = setOf(HIGH, CRITICAL)
    }
}
