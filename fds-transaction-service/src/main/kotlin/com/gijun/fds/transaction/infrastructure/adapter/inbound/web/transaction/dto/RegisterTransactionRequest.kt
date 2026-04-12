package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto

import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionCommand
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class RegisterTransactionRequest(
    @field:NotBlank @field:Size(max = 36) val transactionId: String,
    @field:NotBlank @field:Size(max = 20) val userId: String,
    @field:NotBlank @field:Size(min = 13, max = 19) val cardNumber: String,
    @field:DecimalMin("0.01") val amount: BigDecimal,
    @field:NotBlank @field:Size(min = 3, max = 3) val currency: String,
    @field:NotBlank @field:Size(max = 100) val merchantName: String,
    @field:NotBlank @field:Size(max = 30) val merchantCategory: String,
    @field:NotBlank @field:Size(min = 3, max = 3) val country: String,
    @field:NotBlank @field:Size(max = 50) val city: String,
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0") val latitude: Double,
    @field:DecimalMin("-180.0") @field:DecimalMax("180.0") val longitude: Double,
) {
    fun toCommand(): RegisterTransactionCommand = RegisterTransactionCommand(
        transactionId = transactionId,
        userId = userId,
        cardNumber = cardNumber,
        amount = amount,
        currency = currency,
        merchantName = merchantName,
        merchantCategory = merchantCategory,
        country = country,
        city = city,
        latitude = latitude,
        longitude = longitude,
    )
}
