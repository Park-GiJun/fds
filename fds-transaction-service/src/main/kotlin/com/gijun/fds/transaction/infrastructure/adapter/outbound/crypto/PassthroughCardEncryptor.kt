package com.gijun.fds.transaction.infrastructure.adapter.outbound.crypto

import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!prod")
class PassthroughCardEncryptor : CardEncryptor {
    override fun encrypt(plain: String): String = plain
    override fun decrypt(cipher: String): String = cipher
}
