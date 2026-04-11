package com.gijun.fds.common.event

import java.time.Instant

data class DetectionResultEvent(
    val detectionId: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,
    val ruleNames: List<String>,
    val riskScore: Int,
    val timestamp: Instant,
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}
