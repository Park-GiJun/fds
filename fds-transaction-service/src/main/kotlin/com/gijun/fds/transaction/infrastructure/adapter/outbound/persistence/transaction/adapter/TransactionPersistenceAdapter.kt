package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.adapter

import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import com.gijun.fds.transaction.application.port.outbound.TransactionRepository
import com.gijun.fds.transaction.domain.model.Transaction
import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity.TransactionEntity
import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.repository.TransactionJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionPersistenceAdapter(
    private val transactionJpaRepository: TransactionJpaRepository,
    private val cardEncryptor: CardEncryptor,
) : TransactionRepository {

    @Transactional
    override fun save(transaction: Transaction): Transaction {
        val entity = TransactionEntity.fromDomain(transaction, encryptedCardNumber = cardEncryptor.encrypt(transaction.cardNumber))
        return transactionJpaRepository.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByTransactionId(transactionId: String): Transaction? =
        transactionJpaRepository.findByTransactionId(transactionId)?.toDomain()

    @Transactional(readOnly = true)
    override fun existsByTransactionId(transactionId: String): Boolean =
        transactionJpaRepository.existsByTransactionId(transactionId)
}
