package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.model.Transaction
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.vo.CountryCode
import com.gijun.fds.transaction.domain.vo.CurrencyCode
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "transaction_id", unique = true, nullable = false, length = 36)
    val transactionId: String,

    @Column(name = "user_id", nullable = false, length = 20)
    val userId: String,

    @Column(name = "encrypted_card_number", nullable = false, length = 256)
    val encryptedCardNumber: String,

    @Column(name = "masked_card_number", nullable = false, length = 20)
    val maskedCardNumber: String,

    @Column(nullable = false, precision = 18, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Column(name = "merchant_name", nullable = false, length = 100)
    val merchantName: String,

    @Column(name = "merchant_category", nullable = false, length = 30)
    val merchantCategory: String,

    @Column(nullable = false, length = 3)
    val country: String,

    @Column(nullable = false, length = 50)
    val city: String,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TransactionStatus = TransactionStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    var riskLevel: RiskLevel? = null,

    @Column(name = "risk_score")
    var riskScore: Int? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    fun toDomain(): Transaction = Transaction(
        transactionId = transactionId,
        userId = userId,
        maskedCardNumber = maskedCardNumber,
        encryptedCardNumber = encryptedCardNumber,
        amount = amount,
        currency = CurrencyCode(currency),
        merchantName = merchantName,
        merchantCategory = merchantCategory,
        country = CountryCode(country),
        city = city,
        latitude = latitude,
        longitude = longitude,
        status = status,
        riskLevel = riskLevel,
        riskScore = riskScore,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun updateFromDomain(domain: Transaction) {
        status = domain.status
        riskLevel = domain.riskLevel
        riskScore = domain.riskScore
        updatedAt = domain.updatedAt
    }

    companion object {
        fun fromDomain(domain: Transaction) = TransactionEntity(
            transactionId = domain.transactionId,
            userId = domain.userId,
            encryptedCardNumber = domain.encryptedCardNumber,
            maskedCardNumber = domain.maskedCardNumber,
            amount = domain.amount,
            currency = domain.currency.value,
            merchantName = domain.merchantName,
            merchantCategory = domain.merchantCategory,
            country = domain.country.value,
            city = domain.city,
            latitude = domain.latitude,
            longitude = domain.longitude,
            status = domain.status,
            riskLevel = domain.riskLevel,
            riskScore = domain.riskScore,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }
}
