package com.gijun.fds.common.security

object CardMasking {

    fun mask(cardNumber: String): String {
        if (cardNumber.length < 8) return "****"
        val last4 = cardNumber.takeLast(4)
        val masked = "*".repeat(cardNumber.length - 4)
        return masked + last4
    }
}
