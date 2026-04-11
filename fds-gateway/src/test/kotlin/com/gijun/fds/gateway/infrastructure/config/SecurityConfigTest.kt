package com.gijun.fds.gateway.infrastructure.config

import com.gijun.fds.gateway.support.AbstractSecurityTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SecurityConfigTest : AbstractSecurityTest() {

    @Test
    fun `actuator health는 인증 없이 접근 가능하다`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }

    @Test
    fun `루트 경로는 인증 없이 접근 가능하다`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
    }

    @Test
    fun `API 엔드포인트는 미인증 시 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/transactions/test"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `미등록 경로는 차단된다`() {
        mockMvc.perform(get("/unknown/path"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `actuator info는 인증 없이 접근 가능하다`() {
        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk)
    }

    @Test
    fun `detections API는 미인증 시 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/detections/test"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `alerts API는 미인증 시 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/alerts/test"))
            .andExpect(status().isUnauthorized)
    }
}
