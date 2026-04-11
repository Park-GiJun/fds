package com.gijun.fds.transaction.domain.model

import com.gijun.fds.common.domain.RiskLevel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class DetectionResultTest {

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun `정상 범위 riskScore(0)로 생성 가능하다`() {
        val result = DetectionResult(
            detectionId = "det-001", transactionId = "tx-001",
            userId = "USER_00001", riskLevel = RiskLevel.LOW,
            riskScore = 0, triggeredRules = emptyList(), detectedAt = now,
        )
        result.riskScore shouldBe 0
    }

    @Test
    fun `정상 범위 riskScore(100)로 생성 가능하다`() {
        val result = DetectionResult(
            detectionId = "det-002", transactionId = "tx-002",
            userId = "USER_00002", riskLevel = RiskLevel.CRITICAL,
            riskScore = 100, triggeredRules = listOf("HighAmountRule"), detectedAt = now,
        )
        result.riskScore shouldBe 100
    }

    @Test
    fun `riskScore 음수면 생성 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            DetectionResult(
                detectionId = "det-003", transactionId = "tx-003",
                userId = "USER_00003", riskLevel = RiskLevel.LOW,
                riskScore = -1, triggeredRules = emptyList(), detectedAt = now,
            )
        }
    }

    @Test
    fun `riskScore 101이면 생성 시 예외가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            DetectionResult(
                detectionId = "det-004", transactionId = "tx-004",
                userId = "USER_00004", riskLevel = RiskLevel.HIGH,
                riskScore = 101, triggeredRules = emptyList(), detectedAt = now,
            )
        }
    }

    @Test
    fun `triggeredRules가 정확히 저장된다`() {
        val rules = listOf("HighAmountRule", "RapidSuccessionRule")
        val result = DetectionResult(
            detectionId = "det-005", transactionId = "tx-005",
            userId = "USER_00005", riskLevel = RiskLevel.HIGH,
            riskScore = 70, triggeredRules = rules, detectedAt = now,
        )
        result.triggeredRules shouldBe rules
    }
}
