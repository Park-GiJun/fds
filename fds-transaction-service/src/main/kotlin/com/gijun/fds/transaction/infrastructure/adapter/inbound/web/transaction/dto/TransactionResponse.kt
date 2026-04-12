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
)

fun Transaction.toResponse(): TransactionResponse = TransactionResponse(
    transactionId = transactionId,
    userId = userId,
    maskedCardNumber = maskedCardNumber,
    amount = amount,
    currency = currency,
    merchantName = merchantName,
    merchantCategory = merchantCategory,
    country = country,
    city = city,
    latitude = latitude,
    longitude = longitude,
    status = status,
    riskLevel = riskLevel,
    riskScore = riskScore,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
