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
) {
    init {
        require(TRANSACTION_ID_REGEX.matches(transactionId)) { "transactionId must be 8-36 alphanumeric/hyphen chars" }
        require(userId.isNotBlank() && userId.length <= 20) { "userId must be non-blank and <=20 chars" }
        require(CARD_NUMBER_REGEX.matches(cardNumber)) { "cardNumber must be 13-19 digits" }
        require(amount > BigDecimal.ZERO) { "amount must be positive" }
        require(ISO_3_UPPER.matches(currency)) { "currency must be ISO 4217 (3 uppercase letters)" }
        require(ISO_3_UPPER.matches(country)) { "country must be ISO 3166-1 alpha-3 (3 uppercase letters)" }
        require(merchantName.isNotBlank()) { "merchantName must be non-blank" }
        require(merchantCategory.isNotBlank()) { "merchantCategory must be non-blank" }
        require(city.isNotBlank()) { "city must be non-blank" }
        require(latitude in -90.0..90.0) { "latitude must be in [-90, 90]" }
        require(longitude in -180.0..180.0) { "longitude must be in [-180, 180]" }
    }

    companion object {
        private val TRANSACTION_ID_REGEX = Regex("^[A-Za-z0-9-]{8,36}$")
        private val CARD_NUMBER_REGEX = Regex("^[0-9]{13,19}$")
        private val ISO_3_UPPER = Regex("^[A-Z]{3}$")
    }
}
