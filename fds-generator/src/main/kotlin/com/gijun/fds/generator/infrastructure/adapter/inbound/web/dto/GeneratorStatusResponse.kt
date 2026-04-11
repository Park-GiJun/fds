package com.gijun.fds.generator.infrastructure.adapter.inbound.web.dto

data class GeneratorStatusResponse(
    val running: Boolean,
    val totalSent: Long,
    val totalFailed: Long,
    val configuredRate: Int,
)
