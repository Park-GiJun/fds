package com.gijun.fds.generator.domain.model

import com.gijun.fds.common.security.CardMasking
import java.math.BigDecimal
import java.time.Instant

data class TransactionData(
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
) {
    override fun toString(): String =
        "TransactionData(transactionId=$transactionId, userId=$userId, " +
            "cardNumber=${CardMasking.mask(cardNumber)}, amount=$amount, currency=$currency, " +
            "merchantName=$merchantName, merchantCategory=$merchantCategory, " +
            "country=$country, city=$city)"
}

enum class FraudType {
    HIGH_AMOUNT,
    RAPID_SUCCESSION,
    FOREIGN_AFTER_DOMESTIC,
    MIDNIGHT,
}

data class GeneratorStatus(
    val running: Boolean,
    val totalSent: Long,
    val totalFailed: Long,
    val configuredRate: Int,
)
