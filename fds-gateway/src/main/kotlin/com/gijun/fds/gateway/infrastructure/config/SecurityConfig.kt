package com.gijun.fds.gateway.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize("/actuator/health", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/", permitAll)
                authorize("/api/v1/transactions/**", authenticated)
                authorize("/api/v1/detections/**", authenticated)
                authorize("/api/v1/alerts/**", authenticated)
                authorize(anyRequest, denyAll)
            }
            httpBasic { }
        }
        return http.build()
    }
}
