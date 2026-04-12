package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.adapter

import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.repository.DetectionResultJpaRepository

class DetectionResultPersistenceAdapter(
    private val detectionResultJpaRepository: DetectionResultJpaRepository
) {
}