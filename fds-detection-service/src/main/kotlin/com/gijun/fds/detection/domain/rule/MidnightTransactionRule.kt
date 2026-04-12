package com.gijun.fds.detection.domain.rule

import com.gijun.fds.detection.domain.model.DetectionContext
import com.gijun.fds.detection.domain.model.DetectionRule
import com.gijun.fds.detection.domain.model.RuleResult
import java.math.BigDecimal
import java.time.ZoneId

class MidnightTransactionRule : DetectionRule {
    override val name = "MIDNIGHT_HIGH_AMOUNT"

    override fun evaluate(context: DetectionContext): RuleResult {
        val hour = context.occurredAt.atZone(KST).hour
        val isMidnight = hour in MIDNIGHT_START_HOUR until MIDNIGHT_END_HOUR
        val isHighAmount = context.amount >= AMOUNT_THRESHOLD
        val triggered = isMidnight && isHighAmount
        return RuleResult(
            ruleName = name,
            triggered = triggered,
            score = if (triggered) SCORE else 0,
            reason = if (triggered)
                "새벽(${hour}시 KST) + 고액(${context.amount})"
            else "",
        )
    }

    companion object {
        private val KST = ZoneId.of("Asia/Seoul")
        private const val MIDNIGHT_START_HOUR = 0
        private const val MIDNIGHT_END_HOUR = 5
        private val AMOUNT_THRESHOLD = BigDecimal("500000")
        private const val SCORE = 25
    }
}
