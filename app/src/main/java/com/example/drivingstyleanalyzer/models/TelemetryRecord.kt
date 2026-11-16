package com.example.drivingstyleanalyzer.models

enum class TelemetryType { ACCELERATION, BRAKING, TURN }

data class TelemetryRecord(
    val type: TelemetryType,
    val acceleration: Acceleration,
    val timestampMs: Long
)
