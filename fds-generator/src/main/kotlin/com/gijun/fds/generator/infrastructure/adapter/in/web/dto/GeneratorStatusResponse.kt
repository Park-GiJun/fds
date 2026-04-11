package com.gijun.fds.generator.infrastructure.adapter.`in`.web.dto

data class GeneratorStatusResponse(
    val running: Boolean,
    val totalSent: Long,
    val totalFailed: Long,
    val configuredRate: Int,
)
