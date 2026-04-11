package com.gijun.fds.gateway.infrastructure.adapter.`in`.filter

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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
        private val tokens = AtomicInteger(maxTokens)
        private val lastRefill = AtomicLong(System.currentTimeMillis())

        fun tryConsume(): Boolean {
            refillIfNeeded()
            while (true) {
                val current = tokens.get()
                if (current <= 0) return false
                if (tokens.compareAndSet(current, current - 1)) return true
            }
        }

        private fun refillIfNeeded() {
            val now = System.currentTimeMillis()
            val last = lastRefill.get()
            if (now - last >= refillIntervalMs) {
                if (lastRefill.compareAndSet(last, now)) {
                    tokens.getAndSet(maxTokens)
                }
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
