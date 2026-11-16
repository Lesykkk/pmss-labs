package com.example.drivingstyleanalyzer.services.ble

import com.example.drivingstyleanalyzer.integrations.ble.BleDevice
import com.example.drivingstyleanalyzer.integrations.ble.ConnectionState

data class LogEntry(val timestampMs: Long, val message: String)

data class BleState(
    val connection: ConnectionState = ConnectionState.DISCONNECTED,
    val devices: List<BleDevice> = emptyList(),
    val connectedDevice: BleDevice? = null,
    val log: List<LogEntry> = emptyList()
) {
    val isLinked: Boolean
        get() = connection == ConnectionState.CONNECTED || connection == ConnectionState.DATA_TRANSFER
    val canScan: Boolean get() = connection == ConnectionState.DISCONNECTED
    val canConnect: Boolean get() = connection == ConnectionState.DISCONNECTED
    val canDisconnect: Boolean get() = connection == ConnectionState.CONNECTING || isLinked
    val showProgress: Boolean
        get() = connection == ConnectionState.SCANNING || connection == ConnectionState.CONNECTING
}
