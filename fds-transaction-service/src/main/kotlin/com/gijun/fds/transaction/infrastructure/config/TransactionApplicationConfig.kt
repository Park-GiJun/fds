package com.gijun.fds.transaction.infrastructure.config

import com.gijun.fds.transaction.application.handler.TransactionHandler
import com.gijun.fds.transaction.application.port.outbound.TransactionRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TransactionApplicationConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun transactionHandler(
        transactionRepository: TransactionRepository,
        clock: Clock,
    ): TransactionHandler = TransactionHandler(transactionRepository, clock)
}
