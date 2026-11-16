package com.example.drivingstyleanalyzer.services.monitoring

import com.example.drivingstyleanalyzer.integrations.sensors.AccelerometerReader
import com.example.drivingstyleanalyzer.integrations.sensors.AccelerometerSample
import com.example.drivingstyleanalyzer.models.Acceleration
import com.example.drivingstyleanalyzer.models.TelemetryRecord
import com.example.drivingstyleanalyzer.models.TelemetryType
import com.example.drivingstyleanalyzer.repositories.telemetry.TelemetryRepository
import com.example.drivingstyleanalyzer.services.ble.BleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SensorStatus { WAITING, ACTIVE, PAUSED, MISSING, ERROR }

data class MonitoringState(
    val acceleration: Acceleration = Acceleration(),
    val analysis: DrivingAnalysis = DrivingAnalysis(),
    val sensorStatus: SensorStatus = SensorStatus.WAITING
)

class MonitoringService(
    private val accelerometer: AccelerometerReader,
    private val telemetry: TelemetryRepository,
    private val ble: BleService,
    private val scope: CoroutineScope
) {
    private val analyzer = DrivingAnalyzer()
    private var sensorJob: Job? = null
    private val _state = MutableStateFlow(
        MonitoringState(sensorStatus = if (accelerometer.isAvailable) SensorStatus.WAITING else SensorStatus.MISSING)
    )
    val state = _state.asStateFlow()
    val sync = telemetry.sync

    fun start() {
        if (sensorJob?.isActive == true || !accelerometer.isAvailable) return
        _state.update { it.copy(sensorStatus = SensorStatus.WAITING) }
        sensorJob = scope.launch {
            accelerometer.readings()
                .catch { _state.update { it.copy(sensorStatus = SensorStatus.ERROR) } }
                .collect(::processSample)
        }
    }

    fun stop() {
        sensorJob?.cancel()
        sensorJob = null
        val analysis = analyzer.pause()
        _state.update {
            it.copy(
                analysis = analysis,
                sensorStatus = when (it.sensorStatus) {
                    SensorStatus.MISSING, SensorStatus.ERROR -> it.sensorStatus
                    else -> SensorStatus.PAUSED
                }
            )
        }
    }

    fun resetCounts() {
        val analysis = analyzer.resetCounts()
        _state.update { it.copy(analysis = analysis) }
    }

    private fun processSample(sample: AccelerometerSample) {
        val acceleration = Acceleration(sample.x, sample.y, sample.z)
        val previous = _state.value.analysis
        val analysis = analyzer.analyze(acceleration, sample.timestampNs)
        _state.value = MonitoringState(acceleration, analysis, SensorStatus.ACTIVE)

        val record = TelemetryRecord(TelemetryType.ACCELERATION, acceleration, System.currentTimeMillis())
        telemetry.save(record, sample.timestampNs)
        if (analysis.brakingCount > previous.brakingCount) {
            telemetry.save(record.copy(type = TelemetryType.BRAKING), sample.timestampNs)
            ble.send("BRAKING:${analysis.brakingCount}")
        }
        if (analysis.turnCount > previous.turnCount) {
            telemetry.save(record.copy(type = TelemetryType.TURN), sample.timestampNs)
            ble.send("TURN:${analysis.turnCount}")
        }
    }
}
