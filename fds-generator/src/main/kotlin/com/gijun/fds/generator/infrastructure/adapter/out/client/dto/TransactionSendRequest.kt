package com.gijun.fds.generator.infrastructure.adapter.out.client.dto

import java.math.BigDecimal
import java.time.Instant

data class TransactionSendRequest(
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
    val timestamp: Instant,
)
