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
) {
    init {
        require(riskScore in 0..MAX_RISK_SCORE) { "riskScore must be in [0, $MAX_RISK_SCORE], got $riskScore" }
    }

    companion object {
        const val MAX_RISK_SCORE = 100
    }
}
