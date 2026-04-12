package com.gijun.fds.detection.domain.rule

import com.gijun.fds.detection.domain.model.DetectionContext
import com.gijun.fds.detection.domain.model.DetectionRule
import com.gijun.fds.detection.domain.model.RuleResult
import java.math.BigDecimal

class HighAmountRule : DetectionRule {
    override val name = "HIGH_AMOUNT"

    override fun evaluate(context: DetectionContext): RuleResult {
        val threshold = context.profile.averageAmount.multiply(BigDecimal(MULTIPLIER))
        val triggered = context.amount > threshold
        return RuleResult(
            ruleName = name,
            triggered = triggered,
            score = if (triggered) SCORE else 0,
            reason = if (triggered) "금액 ${context.amount} > 평균의 ${MULTIPLIER}배" else "",
        )
    }
    companion object {
        private const val MULTIPLIER = 5
        private const val SCORE = 40
    }
}