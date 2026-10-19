package com.example.drivingstyleanalyzer.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitorStateTest {
    private fun active(braking: Boolean = false, turning: Boolean = false) = MonitorState(
        analysis = DrivingAnalysis(isBraking = braking, isTurning = turning),
        sensorStatus = SensorStatus.ACTIVE
    )

    @Test
    fun smoothDrivingIsNotAWarning() {
        val state = active()
        assertEquals(StatusKind.SMOOTH, state.statusKind)
        assertFalse(state.isWarning)
    }

    @Test
    fun brakingTurnAndBothAreWarnings() {
        assertEquals(StatusKind.BRAKING, active(braking = true).statusKind)
        assertEquals(StatusKind.TURN, active(turning = true).statusKind)
        assertEquals(StatusKind.BOTH, active(braking = true, turning = true).statusKind)
        assertTrue(active(braking = true).isWarning)
        assertTrue(active(turning = true).isWarning)
        assertTrue(active(braking = true, turning = true).isWarning)
    }

    @Test
    fun sensorProblemsAreWarnings() {
        assertTrue(MonitorState(sensorStatus = SensorStatus.MISSING).isWarning)
        assertTrue(MonitorState(sensorStatus = SensorStatus.ERROR).isWarning)
    }

    @Test
    fun waitingAndPausedAreNeutral() {
        assertFalse(MonitorState(sensorStatus = SensorStatus.WAITING).isWarning)
        assertFalse(MonitorState(sensorStatus = SensorStatus.PAUSED).isWarning)
    }

    @Test
    fun maneuverFlagsAreIgnoredWhileSensorIsNotActive() {
        val paused = MonitorState(
            analysis = DrivingAnalysis(isBraking = true),
            sensorStatus = SensorStatus.PAUSED
        )
        assertEquals(StatusKind.PAUSED, paused.statusKind)
        assertFalse(paused.isWarning)
    }
}
