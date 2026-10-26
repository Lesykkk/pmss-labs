package com.example.drivingstyleanalyzer.repository

const val DEVICE_ID = "driving-analyzer-1"

data class SensorReading(
    val sensorType: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long
)

fun SensorReading.toPayload() = SensorPayload(
    deviceId = DEVICE_ID,
    sensorType = sensorType,
    x = x,
    y = y,
    z = z,
    timestamp = timestamp
)
