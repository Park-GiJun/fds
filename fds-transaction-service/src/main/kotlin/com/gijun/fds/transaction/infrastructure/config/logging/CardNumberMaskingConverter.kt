package com.gijun.fds.transaction.infrastructure.config.logging

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class CardNumberMaskingConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String =
        PanMasker.mask(event.formattedMessage ?: "")
}
