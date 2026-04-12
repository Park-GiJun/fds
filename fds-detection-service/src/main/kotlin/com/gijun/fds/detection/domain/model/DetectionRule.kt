package com.gijun.fds.detection.domain.model

interface DetectionRule {
    val name: String
    fun evaluate(context: DetectionContext): RuleResult
}
