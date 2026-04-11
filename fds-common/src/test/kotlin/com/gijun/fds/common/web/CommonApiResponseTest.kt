package com.gijun.fds.common.web

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class CommonApiResponseTest {

    @Test
    fun `success 응답 생성 시 data가 포함되고 success가 true이다`() {
        // given
        val data = mapOf("key" to "value")

        // when
        val response = CommonApiResponse.success(data)

        // then
        response.success shouldBe true
        response.data shouldBe data
        response.message.shouldBeNull()
        response.errorCode.shouldBeNull()
        response.timestamp.shouldNotBeBlank()
    }

    @Test
    fun `created 응답 생성 시 data가 포함되고 success가 true이다`() {
        // given
        val data = "created-resource"

        // when
        val response = CommonApiResponse.created(data)

        // then
        response.success shouldBe true
        response.data shouldBe "created-resource"
        response.message.shouldBeNull()
    }

    @Test
    fun `error 응답 생성 시 message가 포함되고 success가 false이다`() {
        // when
        val response = CommonApiResponse.error<String>("에러 발생", "ERR_001")

        // then
        response.success shouldBe false
        response.data.shouldBeNull()
        response.message shouldBe "에러 발생"
        response.errorCode shouldBe "ERR_001"
    }

    @Test
    fun `error 응답에서 errorCode가 null일 수 있다`() {
        // when
        val response = CommonApiResponse.error<String>("에러 발생")

        // then
        response.success shouldBe false
        response.message shouldBe "에러 발생"
        response.errorCode.shouldBeNull()
    }

    @Test
    fun `timestamp는 ISO-8601 형식이다`() {
        // when
        val response = CommonApiResponse.success("test")

        // then
        response.timestamp.shouldNotBeBlank()
        response.timestamp.shouldNotBeNull()
    }
}
