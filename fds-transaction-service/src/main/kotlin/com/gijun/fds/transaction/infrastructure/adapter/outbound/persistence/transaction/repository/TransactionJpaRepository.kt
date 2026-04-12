package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.repository

import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionJpaRepository : JpaRepository<TransactionEntity, Long> {
}