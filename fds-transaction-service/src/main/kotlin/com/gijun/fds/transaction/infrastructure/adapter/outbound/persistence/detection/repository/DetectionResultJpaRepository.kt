package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.repository

import com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.detection.entity.DetectionResultEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DetectionResultJpaRepository : JpaRepository<DetectionResultEntity, Long> {
}