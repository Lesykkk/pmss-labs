package com.example.drivingstyleanalyzer.helpers.ble

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockBleTest {
    private val device = BleDevice(BleProfile.DEVICE_NAME, BleProfile.DEVICE_ADDRESS)

    @Test
    fun scannerFindsVirtualDeviceAfterTwoSeconds() = runTest {
        var result: List<BleDevice>? = null
        launch { result = MockBleScanner().scan() }

        advanceTimeBy(1_999)
        runCurrent()
        assertEquals(null, result)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(device), result)
    }

    @Test
    fun connectCompletesAfterDelay() = runTest {
        var connected = false
        val connector = MockBleConnector(connectDelayMs = 1_000L)
        launch {
            connector.connect(device)
            connected = true
        }

        advanceTimeBy(999)
        runCurrent()
        assertFalse(connected)

        advanceTimeBy(1)
        runCurrent()
        assertTrue(connected)
    }

    @Test
    fun sendDataReturnsAckWhenConnected() = runTest {
        val connector = MockBleConnector()
        connector.connect(device)
        var reply: String? = null
        launch { reply = connector.sendData("BRAKING:1") }
        advanceUntilIdle()
        assertEquals("OK", reply)
    }

    @Test
    fun sendDataFailsBeforeConnect() = runTest {
        val error = runCatching { MockBleConnector().sendData("TURN:1") }.exceptionOrNull()
        assertTrue(error is IllegalStateException)
    }

    @Test
    fun sendDataFailsAfterDisconnect() = runTest {
        val connector = MockBleConnector()
        connector.connect(device)
        connector.disconnect()
        val error = runCatching { connector.sendData("TURN:1") }.exceptionOrNull()
        assertTrue(error is IllegalStateException)
    }

    @Test
    fun linkDroppedDuringTransferIsReported() = runTest {
        val connector = MockBleConnector(transferDelayMs = 300L)
        connector.connect(device)
        var error: Throwable? = null
        launch { error = runCatching { connector.sendData("BRAKING:1") }.exceptionOrNull() }
        advanceTimeBy(100)
        runCurrent()
        connector.disconnect()
        advanceUntilIdle()
        assertTrue(error is IllegalStateException)
    }
}
