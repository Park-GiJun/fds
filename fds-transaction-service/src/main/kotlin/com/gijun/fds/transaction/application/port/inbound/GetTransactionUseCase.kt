package com.gijun.fds.transaction.application.port.inbound

import com.gijun.fds.transaction.domain.model.Transaction

interface GetTransactionUseCase {
    fun getByTransactionId(transactionId: String): Transaction
}
