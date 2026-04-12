package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.exception

import com.gijun.fds.common.exception.DomainAccessDeniedException
import com.gijun.fds.common.exception.DomainAlreadyExistsException
import com.gijun.fds.common.exception.DomainAuthenticationRequiredException
import com.gijun.fds.common.exception.DomainConflictException
import com.gijun.fds.common.exception.DomainInvalidStateException
import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.common.exception.DomainValidationException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `DomainNotFoundException → 404`() {
        val r = handler.handleNotFound(DomainNotFoundException("x"))
        r.statusCode shouldBe HttpStatus.NOT_FOUND
        r.body?.errorCode shouldBe "NOT_FOUND"
        r.body?.success shouldBe false
    }

    @Test
    fun `DomainAlreadyExistsException → 409`() {
        val r = handler.handleAlreadyExists(DomainAlreadyExistsException("x"))
        r.statusCode shouldBe HttpStatus.CONFLICT
        r.body?.errorCode shouldBe "ALREADY_EXISTS"
    }

    @Test
    fun `DomainConflictException → 409`() {
        handler.handleConflict(DomainConflictException("x")).statusCode shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `DomainValidationException → 400`() {
        handler.handleValidation(DomainValidationException("x")).statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `DomainInvalidStateException → 422`() {
        handler.handleInvalidState(DomainInvalidStateException("x")).statusCode shouldBe HttpStatus.UNPROCESSABLE_ENTITY
    }

    @Test
    fun `DomainAuthenticationRequiredException → 401`() {
        handler.handleAuth(DomainAuthenticationRequiredException("x")).statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `DomainAccessDeniedException → 403`() {
        handler.handleAccess(DomainAccessDeniedException("x")).statusCode shouldBe HttpStatus.FORBIDDEN
    }

    @Test
    fun `Exception fallback → 500`() {
        val r = handler.handleUnexpected(RuntimeException("boom"))
        r.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        r.body?.errorCode shouldBe "INTERNAL_ERROR"
    }
}
