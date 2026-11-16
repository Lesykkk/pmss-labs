package com.example.drivingstyleanalyzer.ui.monitor

import com.example.drivingstyleanalyzer.integrations.ble.BleDevice
import com.example.drivingstyleanalyzer.models.Acceleration
import com.example.drivingstyleanalyzer.repositories.telemetry.TelemetrySync
import com.example.drivingstyleanalyzer.services.ble.BleState
import com.example.drivingstyleanalyzer.services.monitoring.DrivingAnalysis
import com.example.drivingstyleanalyzer.services.monitoring.SensorStatus

enum class StatusKind { WAITING, PAUSED, MISSING, ERROR, SMOOTH, BRAKING, TURN, BOTH }

data class MonitorState(
    val acceleration: Acceleration = Acceleration(),
    val analysis: DrivingAnalysis = DrivingAnalysis(),
    val sensorStatus: SensorStatus = SensorStatus.WAITING,
    val ble: BleState = BleState(),
    val sync: TelemetrySync = TelemetrySync()
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
    data object CloudOffline : MonitorEvent
}
