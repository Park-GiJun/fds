package com.gijun.fds.common.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RiskLevelTest {

    @Test
    fun `HIGH는 차단 레벨이다`() {
        RiskLevel.HIGH.isBlockLevel() shouldBe true
    }

    @Test
    fun `CRITICAL은 차단 레벨이다`() {
        RiskLevel.CRITICAL.isBlockLevel() shouldBe true
    }

    @Test
    fun `LOW는 차단 레벨이 아니다`() {
        RiskLevel.LOW.isBlockLevel() shouldBe false
    }

    @Test
    fun `MEDIUM은 차단 레벨이 아니다`() {
        RiskLevel.MEDIUM.isBlockLevel() shouldBe false
    }

    @Test
    fun `4개의 레벨이 존재한다`() {
        RiskLevel.entries.size shouldBe 4
    }
}
