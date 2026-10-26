package com.example.drivingstyleanalyzer.monitor

import kotlin.math.sqrt

class UploadThrottle(
    private val intervalMs: Long = 5_000L,
    private val deltaThreshold: Float = 3f,
    private val minGapMs: Long = 500L
) {
    private var lastSentMs: Long? = null
    private var lastSent = Acceleration()

    fun shouldSend(acceleration: Acceleration, nowMs: Long): Boolean {
        val last = lastSentMs
        val elapsed = if (last == null) Long.MAX_VALUE else nowMs - last
        val due = elapsed >= intervalMs || (elapsed >= minGapMs && distance(acceleration, lastSent) >= deltaThreshold)
        if (due) {
            lastSentMs = nowMs
            lastSent = acceleration
        }
        return due
    }

    private fun distance(a: Acceleration, b: Acceleration): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
