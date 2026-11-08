package com.example.drivingstyleanalyzer.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivingstyleanalyzer.DrivingApp
import com.example.drivingstyleanalyzer.helpers.AccelerometerReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitorViewModel(app: Application) : AndroidViewModel(app) {
    private val accelerometer = AccelerometerReader(app)
    private val analyzer = DrivingAnalyzer()

    private val _state = MutableStateFlow(
        MonitorState(sensorStatus = if (accelerometer.isAvailable) SensorStatus.WAITING else SensorStatus.MISSING)
    )
    val state: StateFlow<MonitorState> = _state.asStateFlow()

    private val _events = Channel<MonitorEvent>()
    val events = _events.receiveAsFlow()

    private val ble = BleController(
        scope = viewModelScope,
        current = { _state.value.ble },
        update = { change -> _state.update { it.copy(ble = change(it.ble)) } }
    )

    private val cloud = CloudSyncController(
        scope = viewModelScope,
        repository = (app as DrivingApp).sensorRepository,
        current = { _state.value.cloud },
        update = { change -> _state.update { it.copy(cloud = change(it.cloud)) } },
        notify = { _events.send(it) }
    )

    private var sensorJob: Job? = null

    fun onAction(action: MonitorAction) {
        when (action) {
            MonitorAction.Start -> startSensor()
            MonitorAction.Stop -> stopSensor()
            MonitorAction.ResetClick -> resetCounts()
            MonitorAction.ScanClick -> ble.scan()
            is MonitorAction.ConnectClick -> ble.connect(action.device)
            MonitorAction.DisconnectClick -> ble.disconnect()
        }
    }

    private fun startSensor() {
        if (sensorJob?.isActive == true || !accelerometer.isAvailable) return
        _state.update { it.copy(sensorStatus = SensorStatus.WAITING) }
        sensorJob = viewModelScope.launch {
            accelerometer.readings()
                .catch { _state.update { it.copy(sensorStatus = SensorStatus.ERROR) } }
                .collect { sample ->
                    val acceleration = Acceleration(sample.x, sample.y, sample.z)
                    val previous = _state.value.analysis
                    val analysis = analyzer.analyze(acceleration, sample.timestampNs)
                    _state.update {
                        it.copy(acceleration = acceleration, analysis = analysis, sensorStatus = SensorStatus.ACTIVE)
                    }

                    cloud.onSample(acceleration, sample.timestampNs)
                    if (analysis.brakingCount > previous.brakingCount) ble.send("BRAKING:${analysis.brakingCount}")
                    if (analysis.turnCount > previous.turnCount) ble.send("TURN:${analysis.turnCount}")
                }
        }
    }

    private fun stopSensor() {
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

    private fun resetCounts() {
        val analysis = analyzer.resetCounts()
        _state.update { it.copy(analysis = analysis) }
        viewModelScope.launch { _events.send(MonitorEvent.CountersReset) }
    }
}
