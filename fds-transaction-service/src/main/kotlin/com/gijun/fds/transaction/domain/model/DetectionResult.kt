package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import java.time.Instant

data class DetectionResult(
    val detectionId: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val triggeredRules: List<String>,
    val detectedAt: Instant,
)
