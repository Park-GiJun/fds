package com.gijun.fds.detection.domain.rule

import com.gijun.fds.detection.domain.model.DetectionContext
import com.gijun.fds.detection.domain.model.DetectionRule
import com.gijun.fds.detection.domain.model.RuleResult
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GeoImpossibleTravelRule : DetectionRule {
    override val name = "GEO_IMPOSSIBLE_TRAVEL"

    override fun evaluate(context: DetectionContext): RuleResult {
        val profile = context.profile
        if (profile.lastLatitude == null || profile.lastOccurredAt == null) {
            return RuleResult(name, triggered = false, score = 0, reason = "")
        }
        val distanceKm = haversine(
            profile.lastLatitude, profile.lastLongitude!!,
            context.latitude, context.longitude,
        )
        val elapsedHours = Duration.between(profile.lastOccurredAt, context.occurredAt).toSeconds() / 3600.0
        val speedKmH = if (elapsedHours > 0) distanceKm / elapsedHours else Double.MAX_VALUE
        val triggered = speedKmH > MAX_FEASIBLE_SPEED_KMH
        return RuleResult(
            ruleName = name,
            triggered = triggered,
            score = if (triggered) SCORE else 0,
            reason = if (triggered) "이동 속도 ${speedKmH.toInt()}km/h" else "",
        )
    }

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val MAX_FEASIBLE_SPEED_KMH = 900
        private const val SCORE = 50
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = sin(Δφ / 2).pow(2) +
                cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }
}