package com.gijun.fds.generator.domain.model

import java.math.BigDecimal
import java.time.Instant

@JvmInline
value class CardNumber(private val value: String) {
    val raw: String get() = value
    override fun toString(): String =
        if (value.length >= 13) "${value.take(6)}******${value.takeLast(4)}"
        else "******"
}

data class TransactionData(
    val transactionId: String,
    val userId: String,
    val cardNumber: CardNumber,
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
