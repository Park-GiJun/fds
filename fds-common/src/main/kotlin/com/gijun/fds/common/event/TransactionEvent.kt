package com.gijun.fds.common.event

import com.gijun.fds.common.security.CardMasking
import java.math.BigDecimal
import java.time.Instant

data class TransactionEvent(
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
) {
    companion object {
        fun fromRaw(
            transactionId: String, userId: String, cardNumber: String,
            amount: BigDecimal, currency: String, merchantName: String,
            merchantCategory: String, country: String, city: String,
            latitude: Double, longitude: Double, timestamp: Instant,
        ) = TransactionEvent(
            transactionId = transactionId, userId = userId,
            maskedCardNumber = CardMasking.mask(cardNumber),
            amount = amount, currency = currency, merchantName = merchantName,
            merchantCategory = merchantCategory, country = country, city = city,
            latitude = latitude, longitude = longitude, timestamp = timestamp,
        )
    }
}
