package com.gijun.fds.gateway.infrastructure.adapter.`in`.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthWebAdapter {

    @GetMapping("/")
    fun root(): Map<String, String> = mapOf(
        "service" to "fds-gateway",
        "status" to "UP",
    )
}
