package com.example.drivingstyleanalyzer.monitor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadThrottleTest {
    private val still = Acceleration(0f, 0f, 9.8f)
    private val braking = Acceleration(0f, -12f, 9.8f)

    @Test
    fun firstSampleIsSentImmediately() {
        assertTrue(UploadThrottle().shouldSend(still, 0L))
    }

    @Test
    fun samplesWithinIntervalAreSkipped() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        assertFalse(throttle.shouldSend(still, 1_000L))
        assertFalse(throttle.shouldSend(still, 4_999L))
    }

    @Test
    fun nextSampleIsSentAfterInterval() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        assertTrue(throttle.shouldSend(still, 5_000L))
    }

    @Test
    fun largeChangeSendsEarly() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        assertTrue(throttle.shouldSend(braking, 1_000L))
    }

    @Test
    fun smallChangeDoesNotSendEarly() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        assertFalse(throttle.shouldSend(Acceleration(0.5f, 0.5f, 9.8f), 1_000L))
    }

    @Test
    fun earlySendsAreRateLimited() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        assertFalse(throttle.shouldSend(braking, 100L))
        assertTrue(throttle.shouldSend(braking, 500L))
    }

    @Test
    fun changeIsMeasuredAgainstLastSentSample() {
        val throttle = UploadThrottle()
        throttle.shouldSend(still, 0L)
        throttle.shouldSend(braking, 1_000L)
        assertFalse(throttle.shouldSend(braking, 2_000L))
    }
}
