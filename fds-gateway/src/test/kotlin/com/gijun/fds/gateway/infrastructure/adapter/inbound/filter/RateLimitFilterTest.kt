package com.gijun.fds.gateway.infrastructure.adapter.inbound.filter

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RateLimitFilterTest {

    private lateinit var filter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        filter = RateLimitFilter()
    }

    @Test
    fun `첫 번째 요청은 통과한다`() {
        // given
        val request = MockHttpServletRequest("GET", "/api/test")
        request.remoteAddr = "192.168.1.1"
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        // when
        filter.doFilter(request, response, chain)

        // then
        response.status shouldBe HttpStatus.OK.value()
    }

    @Test
    fun `동일 IP에서 제한 이내 요청은 모두 통과한다`() {
        // given
        val ip = "10.0.0.1"

        // when — 100개 요청 전송 (MAX_REQUESTS=1000 이내)
        repeat(100) {
            val request = MockHttpServletRequest("GET", "/api/test")
            request.remoteAddr = ip
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, MockFilterChain())

            // then
            response.status shouldBe HttpStatus.OK.value()
        }
    }

    @Test
    fun `서로 다른 IP는 독립적으로 제한된다`() {
        // given & when — 다른 IP에서 각각 요청
        val response1 = MockHttpServletResponse()
        val request1 = MockHttpServletRequest("GET", "/api/test").apply { remoteAddr = "1.1.1.1" }
        filter.doFilter(request1, response1, MockFilterChain())

        val response2 = MockHttpServletResponse()
        val request2 = MockHttpServletRequest("GET", "/api/test").apply { remoteAddr = "2.2.2.2" }
        filter.doFilter(request2, response2, MockFilterChain())

        // then — 둘 다 통과
        response1.status shouldBe HttpStatus.OK.value()
        response2.status shouldBe HttpStatus.OK.value()
    }

    @Test
    fun `제한 초과 시 429 Too Many Requests를 반환한다`() {
        // given — 1000개 요청으로 토큰 소진
        val ip = "10.0.0.99"
        repeat(1000) {
            val request = MockHttpServletRequest("GET", "/api/test")
            request.remoteAddr = ip
            filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
        }

        // when — 1001번째 요청
        val request = MockHttpServletRequest("GET", "/api/test")
        request.remoteAddr = ip
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, MockFilterChain())

        // then
        response.status shouldBe HttpStatus.TOO_MANY_REQUESTS.value()
    }

    @Test
    fun `제한 초과 시 JSON 에러 응답을 반환한다`() {
        // given — 토큰 소진
        val ip = "10.0.0.100"
        repeat(1000) {
            val request = MockHttpServletRequest("GET", "/api/test")
            request.remoteAddr = ip
            filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
        }

        // when
        val request = MockHttpServletRequest("GET", "/api/test")
        request.remoteAddr = ip
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, MockFilterChain())

        // then
        response.contentType shouldBe "application/json"
        response.contentAsString shouldContain "Too many requests"
        response.contentAsString shouldContain "retryAfterMs"
    }

    @Test
    fun `시간 윈도우 리셋 후 요청이 다시 허용된다`() {
        // given — 토큰 소진
        val ip = "10.0.0.200"
        repeat(1000) {
            val request = MockHttpServletRequest("GET", "/api/test")
            request.remoteAddr = ip
            filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
        }

        // when — 리필 간격(1000ms) 대기 후 요청
        Thread.sleep(1100)
        val request = MockHttpServletRequest("GET", "/api/test")
        request.remoteAddr = ip
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, MockFilterChain())

        // then
        response.status shouldBe HttpStatus.OK.value()
    }
}
