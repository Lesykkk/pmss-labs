package com.example.drivingstyleanalyzer.monitor

import com.example.drivingstyleanalyzer.helpers.ble.BleDevice
import com.example.drivingstyleanalyzer.helpers.ble.ConnectionState

data class Acceleration(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)

enum class SensorStatus { WAITING, ACTIVE, PAUSED, MISSING, ERROR }

enum class StatusKind { WAITING, PAUSED, MISSING, ERROR, SMOOTH, BRAKING, TURN, BOTH }

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

data class MonitorState(
    val acceleration: Acceleration = Acceleration(),
    val analysis: DrivingAnalysis = DrivingAnalysis(),
    val sensorStatus: SensorStatus = SensorStatus.WAITING,
    val ble: BleState = BleState()
) {
    val statusKind: StatusKind
        get() = when (sensorStatus) {
            SensorStatus.WAITING -> StatusKind.WAITING
            SensorStatus.PAUSED -> StatusKind.PAUSED
            SensorStatus.MISSING -> StatusKind.MISSING
            SensorStatus.ERROR -> StatusKind.ERROR
            SensorStatus.ACTIVE -> when {
                analysis.isBraking && analysis.isTurning -> StatusKind.BOTH
                analysis.isBraking -> StatusKind.BRAKING
                analysis.isTurning -> StatusKind.TURN
                else -> StatusKind.SMOOTH
            }
        }

    val isWarning: Boolean
        get() = when (statusKind) {
            StatusKind.ERROR, StatusKind.MISSING, StatusKind.BRAKING, StatusKind.TURN, StatusKind.BOTH -> true
            StatusKind.WAITING, StatusKind.PAUSED, StatusKind.SMOOTH -> false
        }
}

sealed interface MonitorAction {
    data object Start : MonitorAction
    data object Stop : MonitorAction
    data object ResetClick : MonitorAction
    data object ScanClick : MonitorAction
    data class ConnectClick(val device: BleDevice) : MonitorAction
    data object DisconnectClick : MonitorAction
}

sealed interface MonitorEvent {
    data object CountersReset : MonitorEvent
}
