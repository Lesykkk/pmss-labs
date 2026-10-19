package com.example.drivingstyleanalyzer.monitor

data class Acceleration(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)

enum class SensorStatus { WAITING, ACTIVE, PAUSED, MISSING, ERROR }

enum class StatusKind { WAITING, PAUSED, MISSING, ERROR, SMOOTH, BRAKING, TURN, BOTH }

data class MonitorState(
    val acceleration: Acceleration = Acceleration(),
    val analysis: DrivingAnalysis = DrivingAnalysis(),
    val sensorStatus: SensorStatus = SensorStatus.WAITING
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
}

sealed interface MonitorEvent {
    data object CountersReset : MonitorEvent
}
