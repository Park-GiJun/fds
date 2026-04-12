package com.gijun.fds.detection.domain.model

import java.math.BigDecimal
import java.time.Instant

data class DetectionContext(
    val transactionId: String,
    val userId: String,
    val amount: BigDecimal,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val occurredAt: Instant,
    val profile: UserBehaviorProfile,    // 과거 이력 스냅샷
)