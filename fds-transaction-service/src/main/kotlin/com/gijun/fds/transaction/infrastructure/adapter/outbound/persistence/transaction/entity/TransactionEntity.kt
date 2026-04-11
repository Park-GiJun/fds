package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class TransactionEntity(

    @Id
    val id: String,


) {
}