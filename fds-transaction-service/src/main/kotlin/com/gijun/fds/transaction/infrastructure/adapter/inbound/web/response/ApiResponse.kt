package com.gijun.fds.transaction.infrastructure.adapter.inbound.web.response

import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: String = Instant.now().toString(),
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        fun <T> created(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        fun <T> error(message: String, errorCode: String? = null): ApiResponse<T> =
            ApiResponse(success = false, message = message, errorCode = errorCode)
    }
}
