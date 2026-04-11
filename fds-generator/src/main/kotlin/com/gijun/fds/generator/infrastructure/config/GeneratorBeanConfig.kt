package com.gijun.fds.generator.infrastructure.config

import com.gijun.fds.generator.application.port.inbound.GeneratorUseCase
import com.gijun.fds.generator.application.port.outbound.TransactionSendPort
import com.gijun.fds.generator.application.handler.GeneratorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorBeanConfig {

    @Bean(destroyMethod = "shutdown")
    fun generatorService(transactionSendPort: TransactionSendPort): GeneratorUseCase =
        GeneratorHandler(transactionSendPort)
}
