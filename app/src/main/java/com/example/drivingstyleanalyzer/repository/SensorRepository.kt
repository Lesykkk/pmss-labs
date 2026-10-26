package com.example.drivingstyleanalyzer.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SensorRepository(private val api: CloudApi) {

    suspend fun save(reading: SensorReading): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { api.upload(reading.toPayload()) }
        }
}
