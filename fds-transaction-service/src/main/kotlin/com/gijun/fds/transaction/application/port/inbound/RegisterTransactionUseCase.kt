package com.gijun.fds.transaction.application.port.inbound

import com.gijun.fds.transaction.application.dto.command.RegisterTransactionCommand
import com.gijun.fds.transaction.domain.model.Transaction

interface RegisterTransactionUseCase {
    fun register(command: RegisterTransactionCommand): Transaction
}
