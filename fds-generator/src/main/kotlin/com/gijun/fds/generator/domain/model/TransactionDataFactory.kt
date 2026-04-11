package com.gijun.fds.generator.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.random.Random

object TransactionDataFactory {

    private val USERS = (1..500).map { "USER_%05d".format(it) }
    private val CARDS = USERS.map { "4${Random.nextLong(100_000_000_000, 999_999_999_999)}" }

    private val MERCHANTS = listOf(
        MerchantInfo("스타벅스", "CAFE", "KR", "서울", 37.5665, 126.9780),
        MerchantInfo("이마트", "GROCERY", "KR", "서울", 37.5140, 127.0565),
        MerchantInfo("CGV", "ENTERTAINMENT", "KR", "서울", 37.5013, 127.0396),
        MerchantInfo("올리브영", "HEALTH", "KR", "서울", 37.5547, 126.9706),
        MerchantInfo("쿠팡", "ONLINE", "KR", "서울", 37.5085, 127.0614),
        MerchantInfo("배달의민족", "FOOD_DELIVERY", "KR", "서울", 37.5172, 127.0473),
        MerchantInfo("GS25", "CONVENIENCE", "KR", "서울", 37.5729, 126.9794),
        MerchantInfo("현대백화점", "DEPARTMENT", "KR", "서울", 37.5260, 127.0404),
        MerchantInfo("교보문고", "BOOKSTORE", "KR", "서울", 37.5701, 126.9812),
        MerchantInfo("네이버페이", "ONLINE", "KR", "성남", 37.3595, 127.1053),
        MerchantInfo("Amazon", "ONLINE", "US", "Seattle", 47.6062, -122.3321),
        MerchantInfo("Walmart", "GROCERY", "US", "New York", 40.7128, -74.0060),
        MerchantInfo("McDonald's", "FOOD", "JP", "Tokyo", 35.6762, 139.6503),
        MerchantInfo("Louis Vuitton", "LUXURY", "FR", "Paris", 48.8566, 2.3522),
        MerchantInfo("Harrods", "DEPARTMENT", "GB", "London", 51.4994, -0.1632),
    )

    private val NORMAL_AMOUNT_RANGES = mapOf(
        "CAFE" to 3_000..15_000,
        "GROCERY" to 10_000..200_000,
        "ENTERTAINMENT" to 10_000..30_000,
        "HEALTH" to 5_000..50_000,
        "ONLINE" to 5_000..300_000,
        "FOOD_DELIVERY" to 15_000..50_000,
        "CONVENIENCE" to 1_000..30_000,
        "DEPARTMENT" to 30_000..500_000,
        "BOOKSTORE" to 10_000..50_000,
        "FOOD" to 5_000..20_000,
        "LUXURY" to 500_000..5_000_000,
    )

    fun createNormal(): TransactionData {
        val userIndex = Random.nextInt(USERS.size)
        val merchant = MERCHANTS.random()
        val amountRange = NORMAL_AMOUNT_RANGES[merchant.category] ?: 5_000..50_000

        return TransactionData(
            transactionId = UUID.randomUUID().toString(),
            userId = USERS[userIndex],
            cardNumber = CARDS[userIndex],
            amount = BigDecimal(Random.nextInt(amountRange.first, amountRange.last)),
            currency = if (merchant.country == "KR") "KRW" else "USD",
            merchantName = merchant.name,
            merchantCategory = merchant.category,
            country = merchant.country,
            city = merchant.city,
            latitude = merchant.latitude + Random.nextDouble(-0.01, 0.01),
            longitude = merchant.longitude + Random.nextDouble(-0.01, 0.01),
            timestamp = Instant.now(),
        )
    }

    fun createSuspicious(type: FraudType): TransactionData {
        val userIndex = Random.nextInt(USERS.size)
        val base = createNormal().copy(
            transactionId = UUID.randomUUID().toString(),
            userId = USERS[userIndex],
            cardNumber = CARDS[userIndex],
            timestamp = Instant.now(),
        )

        return when (type) {
            FraudType.HIGH_AMOUNT -> base.copy(
                amount = BigDecimal(Random.nextInt(3_000_000, 10_000_000)),
            )

            FraudType.RAPID_SUCCESSION -> base

            FraudType.FOREIGN_AFTER_DOMESTIC -> {
                val foreign = MERCHANTS.filter { it.country != "KR" }.random()
                base.copy(
                    merchantName = foreign.name,
                    merchantCategory = foreign.category,
                    country = foreign.country,
                    city = foreign.city,
                    latitude = foreign.latitude,
                    longitude = foreign.longitude,
                    amount = BigDecimal(Random.nextInt(100_000, 2_000_000)),
                )
            }

            FraudType.MIDNIGHT -> base.copy(
                amount = BigDecimal(Random.nextInt(500_000, 3_000_000)),
            )
        }
    }

    private data class MerchantInfo(
        val name: String,
        val category: String,
        val country: String,
        val city: String,
        val latitude: Double,
        val longitude: Double,
    )
}
