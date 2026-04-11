package com.gijun.fds.generator.infrastructure.adapter.`in`.web

import com.gijun.fds.generator.application.port.`in`.GeneratorUseCase
import com.gijun.fds.generator.domain.model.GeneratorStatus
import com.gijun.fds.generator.infrastructure.adapter.`in`.web.dto.GeneratorStatusResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/generator")
class GeneratorWebAdapter(
    private val generatorUseCase: GeneratorUseCase,
) {

    @PostMapping("/start")
    fun start(
        @RequestParam(defaultValue = "100") rate: Int,
        @RequestParam(defaultValue = "0.05") fraudRatio: Double,
    ): GeneratorStatusResponse {
        generatorUseCase.start(rate, fraudRatio)
        return generatorUseCase.getStatus().toResponse()
    }

    @PostMapping("/stop")
    fun stop(): GeneratorStatusResponse {
        generatorUseCase.stop()
        return generatorUseCase.getStatus().toResponse()
    }

    @PostMapping("/burst")
    fun burst(
        @RequestParam(defaultValue = "10000") count: Int,
        @RequestParam(defaultValue = "0.3") fraudRatio: Double,
    ): Map<String, Any> {
        generatorUseCase.burst(count, fraudRatio)
        return mapOf(
            "message" to "Burst started",
            "count" to count,
            "fraudRatio" to fraudRatio,
        )
    }

    @GetMapping("/status")
    fun status(): GeneratorStatusResponse = generatorUseCase.getStatus().toResponse()

    private fun GeneratorStatus.toResponse() = GeneratorStatusResponse(
        running = running,
        totalSent = totalSent,
        totalFailed = totalFailed,
        configuredRate = configuredRate,
    )
}
