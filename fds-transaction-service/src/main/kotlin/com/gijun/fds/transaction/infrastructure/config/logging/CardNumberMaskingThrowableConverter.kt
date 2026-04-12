package com.gijun.fds.transaction.infrastructure.config.logging

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class CardNumberMaskingThrowableConverter : ThrowableProxyConverter() {
    override fun convert(event: ILoggingEvent): String =
        PanMasker.mask(super.convert(event))
}
