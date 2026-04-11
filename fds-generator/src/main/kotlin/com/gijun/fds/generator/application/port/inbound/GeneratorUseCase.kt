package com.gijun.fds.generator.application.port.inbound

import com.gijun.fds.generator.domain.model.GeneratorStatus

interface GeneratorUseCase {
    fun start(rate: Int, fraudRatio: Double)
    fun stop()
    fun burst(count: Int, fraudRatio: Double)
    fun getStatus(): GeneratorStatus
}
