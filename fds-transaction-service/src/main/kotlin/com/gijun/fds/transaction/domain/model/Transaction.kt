package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val transactionId: String,
    val userId: String,
    val cardNumber: String,
    val maskedCardNumber: String,
    val amount: BigDecimal,
    val currency: String,
    val merchantName: String,
    val merchantCategory: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val riskLevel: RiskLevel? = null,
    val riskScore: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun applyDetectionResult(riskLevel: RiskLevel, riskScore: Int, now: Instant): Transaction {
        require(riskScore in 0..MAX_RISK_SCORE) { "riskScore must be in [0, $MAX_RISK_SCORE], got $riskScore" }
        return copy(
            status = if (RiskLevel.isBlockLevel(riskLevel)) TransactionStatus.BLOCKED else TransactionStatus.APPROVED,
            riskLevel = riskLevel,
            riskScore = riskScore.coerceAtMost(MAX_RISK_SCORE),
            updatedAt = now,
        )
    }

    fun markSuspicious(now: Instant): Transaction {
        require(status == TransactionStatus.PENDING) { "SUSPICIOUS 전환은 PENDING 상태에서만 가능합니다. 현재: $status" }
        return copy(
            status = TransactionStatus.SUSPICIOUS,
            updatedAt = now,
        )
    }

    companion object {
        const val MAX_RISK_SCORE = 100
    }
}
