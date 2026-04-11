package com.gijun.fds.generator.infrastructure.adapter.outbound.client

import com.gijun.fds.generator.domain.model.CardNumber
import com.gijun.fds.generator.domain.model.TransactionData
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class KtorTransactionSendAdapterTest {

    private fun createTestTransaction() = TransactionData(
        transactionId = "tx-001",
        userId = "USER_00001",
        cardNumber = CardNumber("4123456789012"),
        amount = BigDecimal(50000),
        currency = "KRW",
        merchantName = "스타벅스",
        merchantCategory = "CAFE",
        country = "KR",
        city = "서울",
        latitude = 37.5665,
        longitude = 126.9780,
        timestamp = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun createMockClient(handler: MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) {
                jackson()
            }
        }
    }

    @Test
    fun `전송 성공 시 true를 반환한다`() = runTest {
        // given
        val client = createMockClient { _ ->
            respond(content = "", status = HttpStatusCode.Created)
        }
        val adapter = KtorTransactionSendAdapter(client, "http://test/api/v1/transactions")

        // when
        val result = adapter.send(createTestTransaction())

        // then
        result shouldBe true
    }

    @Test
    fun `200 OK 응답도 성공으로 처리한다`() = runTest {
        // given
        val client = createMockClient { _ ->
            respond(content = "", status = HttpStatusCode.OK)
        }
        val adapter = KtorTransactionSendAdapter(client, "http://test/api/v1/transactions")

        // when
        val result = adapter.send(createTestTransaction())

        // then
        result shouldBe true
    }

    @Test
    fun `서버 에러(500) 시 false를 반환한다`() = runTest {
        // given
        val client = createMockClient { _ ->
            respond(content = "Internal Server Error", status = HttpStatusCode.InternalServerError)
        }
        val adapter = KtorTransactionSendAdapter(client, "http://test/api/v1/transactions")

        // when
        val result = adapter.send(createTestTransaction())

        // then
        result shouldBe false
    }

    @Test
    fun `클라이언트 에러(400) 시 false를 반환한다`() = runTest {
        // given
        val client = createMockClient { _ ->
            respond(content = "Bad Request", status = HttpStatusCode.BadRequest)
        }
        val adapter = KtorTransactionSendAdapter(client, "http://test/api/v1/transactions")

        // when
        val result = adapter.send(createTestTransaction())

        // then
        result shouldBe false
    }

    @Test
    fun `네트워크 예외 발생 시 false를 반환한다`() = runTest {
        // given
        val client = HttpClient(MockEngine { _ ->
            throw java.io.IOException("Connection refused")
        }) {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val adapter = KtorTransactionSendAdapter(client, "http://test/api/v1/transactions")

        // when
        val result = adapter.send(createTestTransaction())

        // then
        result shouldBe false
    }
}
