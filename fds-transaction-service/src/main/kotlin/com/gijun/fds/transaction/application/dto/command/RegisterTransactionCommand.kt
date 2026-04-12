package com.gijun.fds.transaction.application.dto.command

import java.math.BigDecimal

data class RegisterTransactionCommand(
    val transactionId: String,
    val userId: String,
    val cardNumber: String,
    val amount: BigDecimal,
    val currency: String,
    val country: String,
    val merchantName: String,
    val merchantCategory: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)
