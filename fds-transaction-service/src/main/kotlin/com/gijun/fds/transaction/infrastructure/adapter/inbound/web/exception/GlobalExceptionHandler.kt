package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.exception

import com.gijun.fds.common.exception.DomainAccessDeniedException
import com.gijun.fds.common.exception.DomainAlreadyExistsException
import com.gijun.fds.common.exception.DomainAuthenticationRequiredException
import com.gijun.fds.common.exception.DomainConflictException
import com.gijun.fds.common.exception.DomainInvalidStateException
import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.common.exception.DomainValidationException
import com.gijun.fds.common.web.CommonApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainNotFoundException::class)
    fun handleNotFound(e: DomainNotFoundException) = build(HttpStatus.NOT_FOUND, e.message, "NOT_FOUND")

    @ExceptionHandler(DomainAlreadyExistsException::class)
    fun handleAlreadyExists(e: DomainAlreadyExistsException) = build(HttpStatus.CONFLICT, e.message, "ALREADY_EXISTS")

    @ExceptionHandler(DomainConflictException::class)
    fun handleConflict(e: DomainConflictException) = build(HttpStatus.CONFLICT, e.message, "CONFLICT")

    @ExceptionHandler(DomainValidationException::class)
    fun handleValidation(e: DomainValidationException): ResponseEntity<CommonApiResponse<Nothing>> {
        log.debug("Domain validation failed: {}", e.message)
        return build(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다", "VALIDATION_FAILED")
    }

    @ExceptionHandler(DomainInvalidStateException::class)
    fun handleInvalidState(e: DomainInvalidStateException) = build(HttpStatus.UNPROCESSABLE_ENTITY, e.message, "INVALID_STATE")

    @ExceptionHandler(DomainAuthenticationRequiredException::class)
    fun handleAuth(e: DomainAuthenticationRequiredException) = build(HttpStatus.UNAUTHORIZED, e.message, "AUTH_REQUIRED")

    @ExceptionHandler(DomainAccessDeniedException::class)
    fun handleAccess(e: DomainAccessDeniedException) = build(HttpStatus.FORBIDDEN, e.message, "ACCESS_DENIED")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(e: MethodArgumentNotValidException): ResponseEntity<CommonApiResponse<Nothing>> {
        if (log.isDebugEnabled) {
            log.debug(
                "Bean validation failed: {}",
                e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" },
            )
        }
        val fields = e.bindingResult.fieldErrors.joinToString(", ") { it.field }
        return build(HttpStatus.BAD_REQUEST, "다음 필드의 값이 유효하지 않습니다: $fields", "VALIDATION_FAILED")
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<CommonApiResponse<Nothing>> {
        log.error("Unexpected error", e)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "내부 오류가 발생했습니다", "INTERNAL_ERROR")
    }

    private fun build(status: HttpStatus, message: String?, code: String): ResponseEntity<CommonApiResponse<Nothing>> =
        ResponseEntity.status(status).body(CommonApiResponse.error(message ?: status.reasonPhrase, code))
}
