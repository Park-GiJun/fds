package com.gijun.fds.gateway.infrastructure.adapter.`in`.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class LoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startTime = System.currentTimeMillis()

        filterChain.doFilter(request, response)

        val duration = System.currentTimeMillis() - startTime
        log.info(
            "[GATEWAY] {} {} → {} ({}ms)",
            request.method,
            request.requestURI,
            response.status,
            duration,
        )
    }
}
