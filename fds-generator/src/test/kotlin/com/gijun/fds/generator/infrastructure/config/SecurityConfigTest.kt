package com.gijun.fds.generator.infrastructure.config

import com.gijun.fds.generator.support.AbstractSecurityTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SecurityConfigTest : AbstractSecurityTest() {

    @Test
    fun `actuator health는 인증 없이 접근 가능하다`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }

    @Test
    fun `generator status는 인증 없이 접근 가능하다`() {
        mockMvc.perform(get("/api/v1/generator/status"))
            .andExpect(status().isOk)
    }

    @Test
    fun `generator start는 미인증 시 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/generator/start"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `미등록 경로는 차단된다`() {
        mockMvc.perform(get("/unknown/path"))
            .andExpect(status().isUnauthorized)
    }
}
