package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.common.security.CardMasking
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.vo.CountryCode
import com.gijun.fds.transaction.domain.vo.CurrencyCode
import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val transactionId: String,
    val userId: String,
    val maskedCardNumber: String,
    val amount: BigDecimal,
    val currency: CurrencyCode,
    val merchantName: String,
    val merchantCategory: String,
    val country: CountryCode,
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
        require(status == TransactionStatus.PENDING) { "탐지 결과는 PENDING 상태에서만 적용 가능합니다. 현재: $status" }
        require(riskScore in 0..MAX_RISK_SCORE) { "riskScore must be in [0, $MAX_RISK_SCORE], got $riskScore" }
        return copy(
            status = if (riskLevel.isBlockLevel()) TransactionStatus.BLOCKED else TransactionStatus.APPROVED,
            riskLevel = riskLevel,
            riskScore = riskScore,
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

    fun approveAfterReview(now: Instant): Transaction {
        require(status == TransactionStatus.SUSPICIOUS) { "수동 승인은 SUSPICIOUS 상태에서만 가능합니다. 현재: $status" }
        return copy(
            status = TransactionStatus.APPROVED,
            updatedAt = now,
        )
    }

    fun blockAfterReview(now: Instant): Transaction {
        require(status == TransactionStatus.SUSPICIOUS) { "수동 차단은 SUSPICIOUS 상태에서만 가능합니다. 현재: $status" }
        return copy(
            status = TransactionStatus.BLOCKED,
            updatedAt = now,
        )
    }

    companion object {
        const val MAX_RISK_SCORE = 100

        fun create(
            transactionId: String, userId: String, cardNumber: String,
            amount: BigDecimal, currency: String, merchantName: String,
            merchantCategory: String, country: String, city: String,
            latitude: Double, longitude: Double, now: Instant,
        ) = Transaction(
            transactionId = transactionId, userId = userId,
            maskedCardNumber = CardMasking.mask(cardNumber),
            amount = amount, currency = CurrencyCode(currency),
            merchantName = merchantName, merchantCategory = merchantCategory,
            country = CountryCode(country), city = city,
            latitude = latitude, longitude = longitude,
            createdAt = now, updatedAt = now,
        )
    }
}
