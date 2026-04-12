package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.adapter

import com.gijun.fds.common.exception.DomainAlreadyExistsException
import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import com.gijun.fds.transaction.application.port.outbound.TransactionPersistencePort
import com.gijun.fds.transaction.domain.model.Transaction
import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity.TransactionEntity
import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.repository.TransactionJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class TransactionPersistenceAdapter(
    private val transactionJpaRepository: TransactionJpaRepository,
    private val cardEncryptor: CardEncryptor,
) : TransactionPersistencePort {

    override fun save(transaction: Transaction, plainCardNumber: String): Transaction {
        val entity = TransactionEntity.fromDomain(
            transaction,
            encryptedCardNumber = cardEncryptor.encrypt(plainCardNumber),
        )
        return try {
            transactionJpaRepository.save(entity).toDomain()
        } catch (e: DataIntegrityViolationException) {
            throw DomainAlreadyExistsException("이미 등록된 거래입니다: ${transaction.transactionId}")
        }
    }

    override fun findByTransactionId(transactionId: String): Transaction? =
        transactionJpaRepository.findByTransactionId(transactionId)?.toDomain()

    override fun existsByTransactionId(transactionId: String): Boolean =
        transactionJpaRepository.existsByTransactionId(transactionId)
}
