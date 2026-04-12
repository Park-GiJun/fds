package com.gijun.fds.transaction.application.port.outbound

interface CardEncryptor {
    fun encrypt(plain: String): String
}
