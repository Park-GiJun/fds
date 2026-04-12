package com.gijun.fds.detection.domain.model

import com.gijun.fds.common.domain.RiskLevel
import java.time.Instant

data class Detection(
    val detectionId: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,                  // 0~100, 합산 후 cap
    val triggeredRules: List<String>,
    val detectedAt: Instant,
) {
    companion object {
        fun aggregate(
            detectionId: String, transactionId: String, userId: String,
            results: List<RuleResult>, now: Instant,
        ): Detection {
            val triggered = results.filter { it.triggered }
            val totalScore = triggered.sumOf { it.score }.coerceAtMost(MAX_RISK_SCORE)
            return Detection(
                detectionId = detectionId,
                transactionId = transactionId,
                userId = userId,
                riskLevel = riskLevelOf(totalScore),
                riskScore = totalScore,
                triggeredRules = triggered.map { it.ruleName },
                detectedAt = now,
            )
        }
        private const val MAX_RISK_SCORE = 100
        private fun riskLevelOf(score: Int): RiskLevel = when {
            score < 30 -> RiskLevel.LOW
            score < 60 -> RiskLevel.MEDIUM
            score < 80 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }
}