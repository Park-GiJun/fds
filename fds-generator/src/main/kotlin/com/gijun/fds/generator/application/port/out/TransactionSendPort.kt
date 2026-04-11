package com.gijun.fds.generator.application.port.out

import com.gijun.fds.generator.domain.model.TransactionData

interface TransactionSendPort {
    suspend fun send(transaction: TransactionData): Boolean
}
