package com.gijun.fds.common.event

import java.math.BigDecimal
import java.time.Instant

data class TransactionEvent(
    val transactionId: String,
    val userId: String,
    val cardNumber: String,
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
