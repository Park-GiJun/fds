package com.gijun.fds.common.security

object CardMasking {

    private const val VISIBLE_PREFIX = 6
    private const val VISIBLE_SUFFIX = 4
    private const val MIN_LENGTH_FOR_MASKING = 13

    fun mask(cardNumber: String): String {
        if (cardNumber.length < MIN_LENGTH_FOR_MASKING) return "******"
        val prefix = cardNumber.take(VISIBLE_PREFIX)
        val suffix = cardNumber.takeLast(VISIBLE_SUFFIX)
        val maskedMiddle = "*".repeat(cardNumber.length - VISIBLE_PREFIX - VISIBLE_SUFFIX)
        return "$prefix$maskedMiddle$suffix"
    }
}
