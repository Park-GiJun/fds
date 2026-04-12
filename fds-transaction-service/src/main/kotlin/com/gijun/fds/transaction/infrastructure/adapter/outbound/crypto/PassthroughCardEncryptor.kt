package com.gijun.fds.transaction.infrastructure.adapter.outbound.crypto

import com.gijun.fds.transaction.application.port.outbound.CardEncryptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "fds.crypto.passthrough", name = ["enabled"], havingValue = "true")
class PassthroughCardEncryptor : CardEncryptor {
    override fun encrypt(plain: String): String = plain
    override fun decrypt(cipher: String): String = cipher
}
