package com.gijun.fds.transaction.application.port.inbound

import com.gijun.fds.transaction.domain.model.Transaction
import java.math.BigDecimal

interface RegisterTransactionUseCase {
    fun register(command: RegisterTransactionCommand): Transaction
}

data class RegisterTransactionCommand(
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
)
