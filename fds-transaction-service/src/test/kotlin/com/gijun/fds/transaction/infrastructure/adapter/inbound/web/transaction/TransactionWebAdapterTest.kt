package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.transaction.application.port.inbound.GetTransactionUseCase
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionUseCase
import com.gijun.fds.transaction.domain.model.Transaction
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.exception.GlobalExceptionHandler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal
import java.time.Instant

class TransactionWebAdapterTest {

    private lateinit var register: RegisterTransactionUseCase
    private lateinit var getUseCase: GetTransactionUseCase
    private lateinit var mockMvc: MockMvc
    private val om = ObjectMapper().also { it.findAndRegisterModules() }

    @BeforeEach
    fun setup() {
        register = mockk()
        getUseCase = mockk()
        mockMvc = MockMvcBuilders
            .standaloneSetup(TransactionWebAdapter(register, getUseCase))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    private fun validBody(overrides: Map<String, Any?> = emptyMap()): String {
        val base = mutableMapOf<String, Any?>(
            "transactionId" to "TX-00000001",
            "userId" to "U-1",
            "cardNumber" to "4111111111111111",
            "amount" to "1000.00",
            "currency" to "USD",
            "merchantName" to "Amazon",
            "merchantCategory" to "RETAIL",
            "country" to "USA",
            "city" to "Seattle",
            "latitude" to 47.6,
            "longitude" to -122.3,
        )
        base.putAll(overrides)
        return om.writeValueAsString(base)
    }

    private fun sampleTx(): Transaction = Transaction.create(
        transactionId = "TX-00000001",
        userId = "U-1",
        plainCardNumber = "4111111111111111",
        encryptedCardNumber = "enc:4111111111111111",
        amount = BigDecimal("1000.00"),
        currency = "USD",
        merchantName = "Amazon",
        merchantCategory = "RETAIL",
        country = "USA",
        city = "Seattle",
        latitude = 47.6,
        longitude = -122.3,
        now = Instant.parse("2026-04-12T00:00:00Z"),
    )

    @Test
    fun `POST 정상 - 201`() {
        every { register.register(any()) } returns sampleTx()
        mockMvc.perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody()),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `POST amount 음수 - 400`() {
        mockMvc.perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody(mapOf("amount" to "-10.00"))),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST currency 소문자 - 400`() {
        mockMvc.perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody(mapOf("currency" to "usd"))),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET 존재 - 200`() {
        every { getUseCase.getByTransactionId("TX-00000001") } returns sampleTx()
        mockMvc.perform(get("/api/v1/transactions/TX-00000001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.transactionId").value("TX-00000001"))
    }

    @Test
    fun `GET 미존재 - 404`() {
        every { getUseCase.getByTransactionId("MISSING") } throws DomainNotFoundException("없음")
        mockMvc.perform(get("/api/v1/transactions/MISSING"))
            .andExpect(status().isNotFound)
    }
}
