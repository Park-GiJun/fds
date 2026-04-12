package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.model.Transaction
import java.math.BigDecimal
import java.time.Instant

data class TransactionResponse(
    val transactionId: String,
    val userId: String,
    val maskedCardNumber: String,
    val amount: BigDecimal,
    val currency: String,
    val merchantName: String,
    val merchantCategory: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val status: TransactionStatus,
    val riskLevel: RiskLevel?,
    val riskScore: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(domain: Transaction): TransactionResponse = TransactionResponse(
            transactionId = domain.transactionId,
            userId = domain.userId,
            maskedCardNumber = domain.maskedCardNumber,
            amount = domain.amount,
            currency = domain.currency,
            merchantName = domain.merchantName,
            merchantCategory = domain.merchantCategory,
            country = domain.country,
            city = domain.city,
            latitude = domain.latitude,
            longitude = domain.longitude,
            status = domain.status,
            riskLevel = domain.riskLevel,
            riskScore = domain.riskScore,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }
}
