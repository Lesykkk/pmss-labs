package com.example.drivingstyleanalyzer.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingAnalyzerTest {
    @Test
    fun stationaryDeviceProducesNoEvents() {
        val analyzer = DrivingAnalyzer()
        val result = analyzer.analyze(Acceleration(), 0L)
        assertEquals(0, result.brakingCount)
        assertEquals(0, result.turnCount)
        assertFalse(result.isBraking)
        assertFalse(result.isTurning)
    }

    @Test
    fun prolongedBrakingCountsOnce() {
        val analyzer = DrivingAnalyzer()
        repeat(200) { analyzer.analyze(Acceleration(y = -4f), it * 20_000_000L) }
        assertEquals(1, analyzer.analyze(Acceleration(y = -4f), 5_000_000_000L).brakingCount)
    }

    @Test
    fun fluctuationsAroundEntryThresholdDoNotRepeatTurn() {
        val analyzer = DrivingAnalyzer()
        analyzer.analyze(Acceleration(x = 3.2f), 0L)
        analyzer.analyze(Acceleration(x = 2.8f), 2_000_000_000L)
        val result = analyzer.analyze(Acceleration(x = 3.2f), 4_000_000_000L)
        assertEquals(1, result.turnCount)
        assertTrue(result.isTurning)
    }

    @Test
    fun newManeuverCountsAfterReleaseAndCooldown() {
        val analyzer = DrivingAnalyzer()
        analyzer.analyze(Acceleration(x = 4f), 0L)
        analyzer.analyze(Acceleration(), 100_000_000L)
        val result = analyzer.analyze(Acceleration(x = -4f), 2_000_000_000L)
        assertEquals(2, result.turnCount)
    }

    @Test
    fun quickRepeatedCrossingIsSuppressed() {
        val analyzer = DrivingAnalyzer()
        analyzer.analyze(Acceleration(y = -4f), 0L)
        analyzer.analyze(Acceleration(), 100_000_000L)
        val result = analyzer.analyze(Acceleration(y = -4f), 200_000_000L)
        assertEquals(1, result.brakingCount)
    }

    @Test
    fun simultaneousBrakingAndTurningCountIndependently() {
        val result = DrivingAnalyzer().analyze(Acceleration(x = -4f, y = -4f), 0L)
        assertEquals(1, result.brakingCount)
        assertEquals(1, result.turnCount)
        assertTrue(result.isBraking)
        assertTrue(result.isTurning)
    }

    @Test
    fun resetDuringHeldManeuverDoesNotCountItAgain() {
        val analyzer = DrivingAnalyzer()
        analyzer.analyze(Acceleration(y = -4f), 0L)
        analyzer.resetCounts()
        val result = analyzer.analyze(Acceleration(y = -4f), 3_000_000_000L)
        assertEquals(0, result.brakingCount)
        assertTrue(result.isBraking)
    }

    @Test
    fun pauseClearsActiveFlagsButRetainsCounts() {
        val analyzer = DrivingAnalyzer()
        analyzer.analyze(Acceleration(x = 4f, y = -4f), 0L)
        val result = analyzer.pause()
        assertEquals(1, result.brakingCount)
        assertEquals(1, result.turnCount)
        assertFalse(result.isBraking)
        assertFalse(result.isTurning)
    }
}
