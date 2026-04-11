package com.gijun.fds.generator.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(
    @Value("\${security.admin.password:admin}") private val adminPassword: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize("/actuator/**", permitAll)
                authorize("/api/v1/generator/status", permitAll)
                authorize("/api/v1/generator/**", hasRole("ADMIN"))
            }
            httpBasic { }
        }
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val admin = User.builder()
            .username("admin")
            .password("{noop}$adminPassword")
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }
}
