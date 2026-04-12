package com.gijun.fds.transaction.application.port.outbound

import com.gijun.fds.transaction.domain.model.Transaction

interface TransactionPersistencePort {
    fun save(transaction: Transaction, plainCardNumber: String): Transaction
    fun findByTransactionId(transactionId: String): Transaction?
    fun existsByTransactionId(transactionId: String): Boolean
}
