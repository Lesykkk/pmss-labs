package com.example.drivingstyleanalyzer.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

class SensorRepository(
    private val api: CloudApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val timeoutMs: Long = UPLOAD_TIMEOUT_MS
) {

    suspend fun save(reading: SensorReading): Result<Unit> =
        withContext(dispatcher) {
            try {
                withTimeout(timeoutMs) { api.upload(reading.toPayload()) }
                Result.success(Unit)
            } catch (e: TimeoutCancellationException) {
                Result.failure(IOException("Cloud write timed out", e))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    companion object {
        const val UPLOAD_TIMEOUT_MS = 4_000L
    }
}
