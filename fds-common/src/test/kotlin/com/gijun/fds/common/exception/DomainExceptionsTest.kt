package com.gijun.fds.common.exception

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DomainExceptionsTest {

    @Test
    fun `DomainNotFoundException 기본 메시지 확인`() {
        val ex = DomainNotFoundException()
        ex.message shouldBe "Resource not found"
        ex.shouldBeInstanceOf<RuntimeException>()
    }

    @Test
    fun `DomainNotFoundException 커스텀 메시지 확인`() {
        val ex = DomainNotFoundException("거래를 찾을 수 없습니다")
        ex.message shouldBe "거래를 찾을 수 없습니다"
    }

    @Test
    fun `DomainValidationException 기본 메시지 확인`() {
        val ex = DomainValidationException()
        ex.message shouldBe "Validation failed"
    }

    @Test
    fun `DomainAlreadyExistsException 기본 메시지 확인`() {
        val ex = DomainAlreadyExistsException()
        ex.message shouldBe "Resource already exists"
    }

    @Test
    fun `DomainConflictException 기본 메시지 확인`() {
        val ex = DomainConflictException()
        ex.message shouldBe "Resource conflict"
    }

    @Test
    fun `DomainAccessDeniedException 기본 메시지 확인`() {
        val ex = DomainAccessDeniedException()
        ex.message shouldBe "Access denied"
    }

    @Test
    fun `DomainAuthenticationRequiredException 기본 메시지 확인`() {
        val ex = DomainAuthenticationRequiredException()
        ex.message shouldBe "Authentication required"
    }

    @Test
    fun `DomainInvalidStateException 기본 메시지 확인`() {
        val ex = DomainInvalidStateException()
        ex.message shouldBe "Invalid state"
    }

    @Test
    fun `모든 도메인 예외는 RuntimeException을 상속한다`() {
        val exceptions = listOf(
            DomainNotFoundException(),
            DomainValidationException(),
            DomainAlreadyExistsException(),
            DomainConflictException(),
            DomainAccessDeniedException(),
            DomainAuthenticationRequiredException(),
            DomainInvalidStateException(),
        )

        exceptions.forEach { ex ->
            ex.shouldBeInstanceOf<RuntimeException>()
        }
    }
}
