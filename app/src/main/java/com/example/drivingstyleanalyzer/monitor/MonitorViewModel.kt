package com.example.drivingstyleanalyzer.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    private var sensorJob: Job? = null

    fun onAction(action: MonitorAction) {
        when (action) {
            MonitorAction.Start -> startSensor()
            MonitorAction.Stop -> stopSensor()
            MonitorAction.ResetClick -> resetCounts()
        }
    }

    private fun startSensor() {
        if (sensorJob?.isActive == true || !accelerometer.isAvailable) return
        _state.update { it.copy(sensorStatus = SensorStatus.WAITING) }
        sensorJob = viewModelScope.launch {
            accelerometer.readings()
                .catch { _state.update { it.copy(sensorStatus = SensorStatus.ERROR) } }
                .collect { sample ->
                    val analysis = analyzer.analyze(Acceleration(sample.x, sample.y, sample.z), sample.timestampNs)
                    _state.update {
                        it.copy(
                            acceleration = Acceleration(sample.x, sample.y, sample.z),
                            analysis = analysis,
                            sensorStatus = SensorStatus.ACTIVE
                        )
                    }
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
