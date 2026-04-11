package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.entity

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.model.DetectionResult
import jakarta.persistence.*
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@Entity
@Table(name = "detection_results")
class DetectionResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "detection_id", unique = true, nullable = false, length = 36)
    val detectionId: String,

    @Column(name = "transaction_id", nullable = false, length = 36)
    val transactionId: String,

    @Column(name = "user_id", nullable = false, length = 20)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 10)
    val riskLevel: RiskLevel,

    @Column(name = "risk_score", nullable = false)
    val riskScore: Int,

    @Column(name = "triggered_rules", nullable = false, columnDefinition = "JSONB")
    val triggeredRules: String,

    @Column(name = "detected_at", nullable = false)
    val detectedAt: Instant = Instant.now(),
) {
    fun toDomain(): DetectionResult = DetectionResult(
        detectionId = detectionId,
        transactionId = transactionId,
        userId = userId,
        riskLevel = riskLevel,
        riskScore = riskScore,
        triggeredRules = parseRules(triggeredRules),
        detectedAt = detectedAt,
    )

    companion object {
        private val objectMapper = ObjectMapper()

        fun fromDomain(domain: DetectionResult) = DetectionResultEntity(
            detectionId = domain.detectionId,
            transactionId = domain.transactionId,
            userId = domain.userId,
            riskLevel = domain.riskLevel,
            riskScore = domain.riskScore,
            triggeredRules = objectMapper.writeValueAsString(domain.triggeredRules),
            detectedAt = domain.detectedAt,
        )

        private fun parseRules(raw: String): List<String> =
            if (raw.isBlank()) emptyList()
            else objectMapper.readValue(raw, objectMapper.typeFactory.constructCollectionType(List::class.java, String::class.java))
    }
}
