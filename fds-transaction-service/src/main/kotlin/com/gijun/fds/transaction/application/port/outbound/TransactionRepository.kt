package com.gijun.fds.transaction.application.port.outbound

import com.gijun.fds.transaction.domain.model.Transaction

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction
    fun findByTransactionId(transactionId: String): Transaction?
    fun existsByTransactionId(transactionId: String): Boolean
}
