package com.gijun.fds.detection.domain.model

data class RuleResult(
    val ruleName: String,
    val triggered: Boolean,
    val score: Int,        // 0~100, 기여 점수
    val reason: String,
)