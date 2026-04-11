package com.gijun.fds.generator.infrastructure.adapter.out.client

import com.gijun.fds.generator.application.port.out.TransactionSendPort
import com.gijun.fds.generator.domain.model.TransactionData
import com.gijun.fds.generator.infrastructure.adapter.out.client.dto.TransactionSendRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KtorTransactionSendAdapter(
    private val httpClient: HttpClient,
    @Value("\${generator.target-url}") private val targetUrl: String,
) : TransactionSendPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun send(transaction: TransactionData): Boolean {
        return try {
            val response = httpClient.post(targetUrl) {
                contentType(ContentType.Application.Json)
                setBody(transaction.toRequest())
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            log.warn("Failed to send transaction {}: {}", transaction.transactionId, e.message)
            false
        }
    }

    private fun TransactionData.toRequest() = TransactionSendRequest(
        transactionId = transactionId,
        userId = userId,
        maskedCardNumber = com.gijun.fds.common.security.CardMasking.mask(cardNumber.raw),
        amount = amount,
        currency = currency,
        merchantName = merchantName,
        merchantCategory = merchantCategory,
        country = country,
        city = city,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
    )
}
