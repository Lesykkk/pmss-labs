package com.example.drivingstyleanalyzer.ui.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivingstyleanalyzer.DrivingApp
import com.example.drivingstyleanalyzer.integrations.sensors.AccelerometerReader
import com.example.drivingstyleanalyzer.repositories.telemetry.SyncStatus
import com.example.drivingstyleanalyzer.services.ble.BleService
import com.example.drivingstyleanalyzer.services.monitoring.MonitoringService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitorViewModel(app: Application) : AndroidViewModel(app) {
    private val ble = BleService(viewModelScope)
    private val monitoring = MonitoringService(AccelerometerReader(app), (app as DrivingApp).telemetry, ble, viewModelScope)

    private val _state = MutableStateFlow(MonitorState(sensorStatus = monitoring.state.value.sensorStatus))
    val state = _state.asStateFlow()
    private val _events = Channel<MonitorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            monitoring.state.collect { sample ->
                _state.update {
                    it.copy(acceleration = sample.acceleration, analysis = sample.analysis, sensorStatus = sample.sensorStatus)
                }
            }
        }
        viewModelScope.launch { ble.state.collect { state -> _state.update { it.copy(ble = state) } } }
        viewModelScope.launch { monitoring.sync.collect { sync -> _state.update { it.copy(sync = sync) } } }
        viewModelScope.launch {
            monitoring.sync.map { it.status == SyncStatus.PENDING }.distinctUntilChanged().collectLatest { pending ->
                if (pending) {
                    delay(2_000L)
                    _events.send(MonitorEvent.CloudOffline)
                }
            }
        }
    }

    fun onAction(action: MonitorAction) {
        when (action) {
            MonitorAction.Start -> monitoring.start()
            MonitorAction.Stop -> monitoring.stop()
            MonitorAction.ResetClick -> {
                monitoring.resetCounts()
                _events.trySend(MonitorEvent.CountersReset)
            }
            MonitorAction.ScanClick -> ble.scan()
            is MonitorAction.ConnectClick -> ble.connect(action.device)
            MonitorAction.DisconnectClick -> ble.disconnect()
        }
    }
}
