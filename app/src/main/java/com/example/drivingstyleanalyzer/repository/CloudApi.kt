package com.example.drivingstyleanalyzer.repository

data class SensorPayload(
    val deviceId: String = "",
    val sensorType: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestamp: Long = 0L
)

interface CloudApi {
    suspend fun upload(payload: SensorPayload)
}
