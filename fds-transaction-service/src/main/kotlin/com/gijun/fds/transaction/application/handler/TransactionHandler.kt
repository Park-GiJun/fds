package com.gijun.fds.transaction.application.handler

import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.transaction.application.dto.command.RegisterTransactionCommand
import com.gijun.fds.transaction.application.port.inbound.GetTransactionUseCase
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionUseCase
import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import com.gijun.fds.transaction.application.port.outbound.TransactionPersistencePort
import com.gijun.fds.transaction.domain.model.Transaction
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

class TransactionHandler(
    private val transactionPersistencePort: TransactionPersistencePort,
    private val cardEncryptor: CardEncryptor,
    private val clock: Clock,
) : RegisterTransactionUseCase, GetTransactionUseCase {

    @Transactional
    override fun register(command: RegisterTransactionCommand): Transaction {
        val encrypted = cardEncryptor.encrypt(command.cardNumber)
        val transaction = Transaction.create(
            transactionId = command.transactionId,
            userId = command.userId,
            plainCardNumber = command.cardNumber,
            encryptedCardNumber = encrypted,
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
        return transactionPersistencePort.save(transaction)
    }

    @Transactional(readOnly = true)
    override fun getByTransactionId(transactionId: String): Transaction =
        transactionPersistencePort.findByTransactionId(transactionId)
            ?: throw DomainNotFoundException("거래를 찾을 수 없습니다: $transactionId")
}
