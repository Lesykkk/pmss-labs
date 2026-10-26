package com.example.drivingstyleanalyzer.repository

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SensorRepositoryTest {
    private val reading = SensorReading("accelerometer", 1f, -12f, 9.8f, 1_700_000_000_000L)

    private class FakeApi(private val failure: Exception? = null) : CloudApi {
        val received = mutableListOf<SensorPayload>()

        override suspend fun upload(payload: SensorPayload) {
            failure?.let { throw it }
            received += payload
        }
    }

    @Test
    fun savePostsReadingAsPayload() = runTest {
        val api = FakeApi()
        val result = SensorRepository(api).save(reading)

        assertTrue(result.isSuccess)
        assertEquals(SensorPayload(DEVICE_ID, "accelerometer", 1f, -12f, 9.8f, 1_700_000_000_000L), api.received.single())
    }

    @Test
    fun networkErrorIsReturnedAsFailure() = runTest {
        val result = SensorRepository(FakeApi(IOException("no network"))).save(reading)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun serverRejectionIsReturnedAsHttpException() = runTest {
        val rejection = HttpException(Response.error<Unit>(500, "".toResponseBody()))
        val result = SensorRepository(FakeApi(rejection)).save(reading)

        assertTrue(result.exceptionOrNull() is HttpException)
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
