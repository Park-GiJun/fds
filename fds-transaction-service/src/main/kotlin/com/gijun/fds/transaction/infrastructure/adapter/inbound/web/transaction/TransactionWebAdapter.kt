package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction

import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.response.ApiResponse
import com.gijun.fds.transaction.application.port.inbound.GetTransactionUseCase
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionUseCase
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto.RegisterTransactionRequest
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto.TransactionResponse
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto.toResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionWebAdapter(
    private val registerTransactionUseCase: RegisterTransactionUseCase,
    private val getTransactionUseCase: GetTransactionUseCase,
) {

    @PostMapping
    fun register(
        @Valid @RequestBody request: RegisterTransactionRequest,
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = registerTransactionUseCase.register(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(transaction.toResponse()))
    }

    @GetMapping("/{transactionId}")
    fun getOne(
        @PathVariable transactionId: String,
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = getTransactionUseCase.getByTransactionId(transactionId)
        return ResponseEntity.ok(ApiResponse.success(transaction.toResponse()))
    }
}
