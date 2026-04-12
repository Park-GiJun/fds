package com.gijun.fds.detection.domain.model

import java.math.BigDecimal
import java.time.Instant

data class UserBehaviorProfile(
    val userId: String,
    val recentTransactionCount: Int,     // 최근 5분 거래 건수
    val averageAmount: BigDecimal,       // 평균 거래 금액
    val lastCountry: String?,
    val lastOccurredAt: Instant?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
)