package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction

import com.gijun.fds.common.web.CommonApiResponse
import com.gijun.fds.transaction.application.port.inbound.GetTransactionUseCase
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionUseCase
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto.RegisterTransactionRequest
import com.gijun.fds.transaction.infrastructure.adapter.inbound.web.transaction.dto.TransactionResponse
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
    ): ResponseEntity<CommonApiResponse<TransactionResponse>> {
        val transaction = registerTransactionUseCase.register(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonApiResponse.created(TransactionResponse.from(transaction)))
    }

    @GetMapping("/{transactionId}")
    fun getOne(
        @PathVariable transactionId: String,
    ): ResponseEntity<CommonApiResponse<TransactionResponse>> {
        val transaction = getTransactionUseCase.getByTransactionId(transactionId)
        return ResponseEntity.ok(CommonApiResponse.success(TransactionResponse.from(transaction)))
    }
}
