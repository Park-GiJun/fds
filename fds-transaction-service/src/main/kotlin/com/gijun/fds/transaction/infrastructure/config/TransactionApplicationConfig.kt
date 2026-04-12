package com.gijun.fds.transaction.infrastructure.config

import com.gijun.fds.transaction.application.handler.TransactionHandler
import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import com.gijun.fds.transaction.application.port.outbound.TransactionPersistencePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TransactionApplicationConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun transactionHandler(
        transactionPersistencePort: TransactionPersistencePort,
        cardEncryptor: CardEncryptor,
        clock: Clock,
    ): TransactionHandler = TransactionHandler(transactionPersistencePort, cardEncryptor, clock)
}
