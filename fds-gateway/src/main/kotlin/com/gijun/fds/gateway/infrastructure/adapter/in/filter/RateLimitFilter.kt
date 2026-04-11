package com.gijun.fds.gateway.infrastructure.adapter.`in`.filter

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val buckets = Caffeine.newBuilder()
        .maximumSize(MAX_BUCKETS)
        .expireAfterAccess(BUCKET_EXPIRE_MINUTES, TimeUnit.MINUTES)
        .build<String, TokenBucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val clientIp = request.remoteAddr
        val bucket = buckets.get(clientIp) { TokenBucket(MAX_REQUESTS, REFILL_INTERVAL_MS) }

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"error":"Too many requests","retryAfterMs":$REFILL_INTERVAL_MS}""")
        }
    }

    private class TokenBucket(private val maxTokens: Int, private val refillIntervalMs: Long) {
        private var tokens = maxTokens
        private var lastRefill = System.currentTimeMillis()

        @Synchronized
        fun tryConsume(): Boolean {
            val now = System.currentTimeMillis()
            if (now - lastRefill >= refillIntervalMs) {
                tokens = maxTokens
                lastRefill = now
            }
            return if (tokens > 0) {
                tokens--
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val MAX_REQUESTS = 1000
        private const val REFILL_INTERVAL_MS = 1000L
        private const val MAX_BUCKETS = 100_000L
        private const val BUCKET_EXPIRE_MINUTES = 10L
    }
}
