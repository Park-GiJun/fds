package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.adapter

import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.repository.TransactionJpaRepository

class TransactionPersistenceAdapter(
    private val transactionJpaRepository: TransactionJpaRepository
) {
}