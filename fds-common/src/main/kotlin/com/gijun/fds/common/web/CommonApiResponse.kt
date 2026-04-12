package com.gijun.fds.common.web

import java.time.Instant

@Deprecated(
    "각 서비스의 infrastructure/adapter/inbound/web/response/ApiResponse 사용. " +
        "doc/policy/api-response.md 참조. fds-common은 Kafka 이벤트 스키마 전용.",
)
data class CommonApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: String = Instant.now().toString()
) {
    companion object {
        fun <T> success(data: T): CommonApiResponse<T> =
            CommonApiResponse(success = true, data = data)

        fun <T> created(data: T): CommonApiResponse<T> =
            CommonApiResponse(success = true, data = data)

        fun <T> error(message: String, errorCode: String? = null): CommonApiResponse<T> =
            CommonApiResponse(success = false, message = message, errorCode = errorCode)
    }
}
