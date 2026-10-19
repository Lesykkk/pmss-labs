package com.example.drivingstyleanalyzer.monitor

import kotlin.math.abs

object DrivingRules {
    const val EVENT_THRESHOLD = 3f
    const val RELEASE_THRESHOLD = 2f
    const val COOLDOWN_NS = 1_500_000_000L
}

class DrivingAnalyzer {
    private var result = DrivingAnalysis()
    private var lastBrakingTime: Long? = null
    private var lastTurnTime: Long? = null

    fun analyze(acceleration: Acceleration, timestampNs: Long): DrivingAnalysis {
        val braking = if (result.isBraking) {
            acceleration.y < -DrivingRules.RELEASE_THRESHOLD
        } else {
            acceleration.y <= -DrivingRules.EVENT_THRESHOLD
        }
        val turning = if (result.isTurning) {
            abs(acceleration.x) > DrivingRules.RELEASE_THRESHOLD
        } else {
            abs(acceleration.x) >= DrivingRules.EVENT_THRESHOLD
        }

        val newBraking = braking && !result.isBraking && ready(lastBrakingTime, timestampNs)
        val newTurn = turning && !result.isTurning && ready(lastTurnTime, timestampNs)
        if (newBraking) lastBrakingTime = timestampNs
        if (newTurn) lastTurnTime = timestampNs

        result = DrivingAnalysis(
            isBraking = braking,
            isTurning = turning,
            brakingCount = result.brakingCount + if (newBraking) 1 else 0,
            turnCount = result.turnCount + if (newTurn) 1 else 0
        )
        return result
    }

    fun resetCounts(): DrivingAnalysis {
        result = result.copy(brakingCount = 0, turnCount = 0)
        lastBrakingTime = null
        lastTurnTime = null
        return result
    }

    fun pause(): DrivingAnalysis {
        result = result.copy(isBraking = false, isTurning = false)
        return result
    }

    private fun ready(previousTime: Long?, now: Long): Boolean =
        previousTime == null || now - previousTime >= DrivingRules.COOLDOWN_NS
}

data class DrivingAnalysis(
    val isBraking: Boolean = false,
    val isTurning: Boolean = false,
    val brakingCount: Int = 0,
    val turnCount: Int = 0
)
