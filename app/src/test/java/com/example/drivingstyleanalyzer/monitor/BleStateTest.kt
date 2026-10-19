package com.example.drivingstyleanalyzer.monitor

import com.example.drivingstyleanalyzer.helpers.ble.ConnectionState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleStateTest {
    @Test
    fun disconnectedAllowsScanAndConnectOnly() {
        val ble = BleState(connection = ConnectionState.DISCONNECTED)
        assertTrue(ble.canScan)
        assertTrue(ble.canConnect)
        assertFalse(ble.canDisconnect)
        assertFalse(ble.showProgress)
    }

    @Test
    fun scanningShowsProgressAndBlocksActions() {
        val ble = BleState(connection = ConnectionState.SCANNING)
        assertTrue(ble.showProgress)
        assertFalse(ble.canScan)
        assertFalse(ble.canConnect)
        assertFalse(ble.canDisconnect)
    }

    @Test
    fun connectingCanBeCancelled() {
        val ble = BleState(connection = ConnectionState.CONNECTING)
        assertTrue(ble.showProgress)
        assertTrue(ble.canDisconnect)
        assertFalse(ble.isLinked)
    }

    @Test
    fun dataTransferCountsAsLinked() {
        val ble = BleState(connection = ConnectionState.DATA_TRANSFER)
        assertTrue(ble.isLinked)
        assertTrue(ble.canDisconnect)
        assertFalse(ble.canScan)
    }
}
