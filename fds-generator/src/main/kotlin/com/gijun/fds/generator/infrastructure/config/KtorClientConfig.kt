package com.gijun.fds.generator.infrastructure.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KtorClientConfig {

    @Bean(destroyMethod = "close")
    fun httpClient(): HttpClient = HttpClient(CIO) {
        engine {
            maxConnectionsCount = 1000
            endpoint {
                maxConnectionsPerRoute = 100
                connectTimeout = 5000
                requestTimeout = 10000
            }
        }
        install(ContentNegotiation) {
            jackson()
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}
