package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.entity

import com.gijun.fds.common.domain.RiskLevel
import com.gijun.fds.transaction.domain.model.DetectionResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.time.Instant

class DetectionResultEntityTest {

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun `fromDomain으로 생성 시 triggeredRules가 JSON 배열로 직렬화된다`() {
        val domain = DetectionResult(
            detectionId = "det-001",
            transactionId = "tx-001",
            userId = "USER_00001",
            riskLevel = RiskLevel.HIGH,
            riskScore = 70,
            triggeredRules = listOf("HighAmountRule", "RapidSuccessionRule"),
            detectedAt = now,
        )

        val entity = DetectionResultEntity.fromDomain(domain)

        entity.triggeredRules shouldContain "HighAmountRule"
        entity.triggeredRules shouldContain "RapidSuccessionRule"
        entity.triggeredRules shouldContain "["
        entity.triggeredRules shouldContain "]"
    }

    @Test
    fun `toDomain 변환 시 triggeredRules가 List로 파싱된다`() {
        val domain = DetectionResult(
            detectionId = "det-002",
            transactionId = "tx-002",
            userId = "USER_00002",
            riskLevel = RiskLevel.CRITICAL,
            riskScore = 95,
            triggeredRules = listOf("GeoTravelRule"),
            detectedAt = now,
        )

        val entity = DetectionResultEntity.fromDomain(domain)
        val restored = entity.toDomain()

        restored.triggeredRules shouldBe listOf("GeoTravelRule")
        restored.riskLevel shouldBe RiskLevel.CRITICAL
        restored.riskScore shouldBe 95
    }

    @Test
    fun `빈 triggeredRules 직렬화 및 복원이 정상 동작한다`() {
        val domain = DetectionResult(
            detectionId = "det-003",
            transactionId = "tx-003",
            userId = "USER_00003",
            riskLevel = RiskLevel.LOW,
            riskScore = 10,
            triggeredRules = emptyList(),
            detectedAt = now,
        )

        val entity = DetectionResultEntity.fromDomain(domain)
        val restored = entity.toDomain()

        restored.triggeredRules shouldBe emptyList()
    }

    @Test
    fun `모든 필드가 정확히 매핑된다`() {
        val domain = DetectionResult(
            detectionId = "det-004",
            transactionId = "tx-004",
            userId = "USER_00004",
            riskLevel = RiskLevel.MEDIUM,
            riskScore = 45,
            triggeredRules = listOf("A", "B", "C"),
            detectedAt = now,
        )

        val entity = DetectionResultEntity.fromDomain(domain)
        val restored = entity.toDomain()

        restored.detectionId shouldBe "det-004"
        restored.transactionId shouldBe "tx-004"
        restored.userId shouldBe "USER_00004"
        restored.riskLevel shouldBe RiskLevel.MEDIUM
        restored.riskScore shouldBe 45
        restored.triggeredRules shouldBe listOf("A", "B", "C")
        restored.detectedAt shouldBe now
    }
}
