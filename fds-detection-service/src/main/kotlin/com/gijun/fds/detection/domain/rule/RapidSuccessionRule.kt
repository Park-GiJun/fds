package com.gijun.fds.detection.domain.rule

import com.gijun.fds.detection.domain.model.DetectionContext
import com.gijun.fds.detection.domain.model.DetectionRule
import com.gijun.fds.detection.domain.model.RuleResult

class RapidSuccessionRule : DetectionRule {
    override val name = "RAPID_SUCCESSION"

    override fun evaluate(context: DetectionContext): RuleResult {
        val recent = context.profile.recentTransactionCount
        val triggered = recent >= THRESHOLD_COUNT
        return RuleResult(
            ruleName = name,
            triggered = triggered,
            score = if (triggered) SCORE else 0,
            reason = if (triggered)
                "최근 ${WINDOW_MINUTES}분 내 ${recent}건 (임계 ${THRESHOLD_COUNT}건)"
            else "",
        )
    }

    companion object {
        private const val WINDOW_MINUTES = 5
        private const val THRESHOLD_COUNT = 5
        private const val SCORE = 30
    }
}
