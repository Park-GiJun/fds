package com.gijun.fds.generator.infrastructure.config

import com.gijun.fds.generator.application.port.out.TransactionSendPort
import com.gijun.fds.generator.application.service.GeneratorService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorBeanConfig {

    @Bean(destroyMethod = "shutdown")
    fun generatorService(transactionSendPort: TransactionSendPort): GeneratorService =
        GeneratorService(transactionSendPort)
}
