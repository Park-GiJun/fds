package com.gijun.fds.transaction.application.handler

import com.gijun.fds.common.exception.DomainAlreadyExistsException
import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.transaction.application.port.inbound.GetTransactionUseCase
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionCommand
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionUseCase
import com.gijun.fds.transaction.application.port.outbound.TransactionRepository
import com.gijun.fds.transaction.domain.model.Transaction
import java.time.Clock
import java.time.Instant

class TransactionHandler(
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) : RegisterTransactionUseCase, GetTransactionUseCase {

    override fun register(command: RegisterTransactionCommand): Transaction {
        if (transactionRepository.existsByTransactionId(command.transactionId)) {
            throw DomainAlreadyExistsException("이미 등록된 거래입니다: ${command.transactionId}")
        }
        val transaction = Transaction.create(
            transactionId = command.transactionId,
            userId = command.userId,
            cardNumber = command.cardNumber,
            amount = command.amount,
            currency = command.currency,
            merchantName = command.merchantName,
            merchantCategory = command.merchantCategory,
            country = command.country,
            city = command.city,
            latitude = command.latitude,
            longitude = command.longitude,
            now = Instant.now(clock),
        )
        return transactionRepository.save(transaction)
    }

    override fun getByTransactionId(transactionId: String): Transaction =
        transactionRepository.findByTransactionId(transactionId)
            ?: throw DomainNotFoundException("거래를 찾을 수 없습니다: $transactionId")
}
