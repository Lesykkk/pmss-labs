package com.example.drivingstyleanalyzer.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SensorRepositoryTest {
    private val reading = SensorReading("accelerometer", 1f, -12f, 9.8f, 1_700_000_000_000L)

    private class FakeApi(
        private val failure: Exception? = null,
        private val delayMs: Long = 0L
    ) : CloudApi {
        val received = mutableListOf<SensorPayload>()

        override suspend fun upload(payload: SensorPayload) {
            if (delayMs > 0) delay(delayMs)
            failure?.let { throw it }
            received += payload
        }
    }

    @Test
    fun savePushesReadingAsPayload() = runTest {
        val api = FakeApi()
        val result = SensorRepository(api, StandardTestDispatcher(testScheduler)).save(reading)

        assertTrue(result.isSuccess)
        assertEquals(SensorPayload(DEVICE_ID, "accelerometer", 1f, -12f, 9.8f, 1_700_000_000_000L), api.received.single())
    }

    @Test
    fun networkErrorIsReturnedAsFailure() = runTest {
        val repository = SensorRepository(FakeApi(IOException("no network")), StandardTestDispatcher(testScheduler))
        val result = repository.save(reading)

        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun otherErrorsAreReturnedUnchanged() = runTest {
        val repository = SensorRepository(FakeApi(IllegalStateException("permission denied")), StandardTestDispatcher(testScheduler))
        val result = repository.save(reading)

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertFalse(result.exceptionOrNull() is IOException)
    }

    @Test
    fun writeThatNeverCompletesTimesOutAsIoError() = runTest {
        val repository = SensorRepository(
            FakeApi(delayMs = 60_000L),
            StandardTestDispatcher(testScheduler),
            timeoutMs = 4_000L
        )
        var result: Result<Unit>? = null
        launch { result = repository.save(reading) }

        advanceTimeBy(3_999)
        runCurrent()
        assertEquals(null, result)

        advanceTimeBy(1)
        runCurrent()
        assertTrue(result?.exceptionOrNull() is IOException)
    }

    @Test
    fun cancellingTheCallerIsNotReportedAsFailure() = runTest {
        val repository = SensorRepository(FakeApi(delayMs = 60_000L), StandardTestDispatcher(testScheduler))
        val completed = CompletableDeferred<Boolean>()
        val job = launch {
            repository.save(reading)
            completed.complete(true)
        }
        runCurrent()
        job.cancel()
        runCurrent()

        assertFalse(completed.isCompleted)
    }

    @Test
    fun toPayloadCopiesAllFields() {
        val payload = reading.toPayload()

        assertEquals(DEVICE_ID, payload.deviceId)
        assertEquals(reading.sensorType, payload.sensorType)
        assertEquals(reading.x, payload.x, 0f)
        assertEquals(reading.y, payload.y, 0f)
        assertEquals(reading.z, payload.z, 0f)
        assertEquals(reading.timestamp, payload.timestamp)
    }
}
