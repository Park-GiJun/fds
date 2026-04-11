package com.gijun.fds.common.domain

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    ;

    companion object {
        private val BLOCK_LEVELS = setOf(HIGH, CRITICAL)

        fun isBlockLevel(level: RiskLevel): Boolean = level in BLOCK_LEVELS
    }
}
