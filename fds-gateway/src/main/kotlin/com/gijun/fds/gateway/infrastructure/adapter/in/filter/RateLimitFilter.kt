package com.gijun.fds.gateway.infrastructure.adapter.`in`.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val clientIp = request.remoteAddr
        val bucket = buckets.computeIfAbsent(clientIp) { TokenBucket(MAX_REQUESTS, REFILL_INTERVAL_MS) }

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"error":"Too many requests","retryAfterMs":$REFILL_INTERVAL_MS}""")
        }
    }

    private class TokenBucket(private val maxTokens: Int, private val refillIntervalMs: Long) {
        private val tokens = AtomicInteger(maxTokens)
        private val lastRefill = AtomicLong(System.currentTimeMillis())

        fun tryConsume(): Boolean {
            refill()
            return tokens.getAndUpdate { if (it > 0) it - 1 else 0 } > 0
        }

        private fun refill() {
            val now = System.currentTimeMillis()
            val last = lastRefill.get()
            val elapsed = now - last

            if (elapsed >= refillIntervalMs && lastRefill.compareAndSet(last, now)) {
                tokens.set(maxTokens)
            }
        }
    }

    companion object {
        private const val MAX_REQUESTS = 1000
        private const val REFILL_INTERVAL_MS = 1000L
    }
}
