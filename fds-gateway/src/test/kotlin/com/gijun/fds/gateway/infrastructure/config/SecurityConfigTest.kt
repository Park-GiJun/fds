package com.gijun.fds.gateway.infrastructure.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

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
