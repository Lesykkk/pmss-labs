package com.example.drivingstyleanalyzer.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivingstyleanalyzer.helpers.AccelerometerReader
import com.example.drivingstyleanalyzer.helpers.ble.BleDevice
import com.example.drivingstyleanalyzer.helpers.ble.BleLogger
import com.example.drivingstyleanalyzer.helpers.ble.BleProfile
import com.example.drivingstyleanalyzer.helpers.ble.ConnectionState
import com.example.drivingstyleanalyzer.helpers.ble.IBleConnector
import com.example.drivingstyleanalyzer.helpers.ble.MockBleConnector
import com.example.drivingstyleanalyzer.helpers.ble.MockBleScanner
import kotlinx.coroutines.CancellationException
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
    private val scanner = MockBleScanner()
    private val connector: IBleConnector = MockBleConnector()
    private val logger = BleLogger()

    private val _state = MutableStateFlow(
        MonitorState(sensorStatus = if (accelerometer.isAvailable) SensorStatus.WAITING else SensorStatus.MISSING)
    )
    val state: StateFlow<MonitorState> = _state.asStateFlow()

    private val _events = Channel<MonitorEvent>()
    val events = _events.receiveAsFlow()

    private var sensorJob: Job? = null
    private var connectJob: Job? = null

    private val outbox = Channel<String>(Channel.UNLIMITED)

    init {
        viewModelScope.launch { for (packet in outbox) transmit(packet) }
    }

    fun onAction(action: MonitorAction) {
        when (action) {
            MonitorAction.Start -> startSensor()
            MonitorAction.Stop -> stopSensor()
            MonitorAction.ResetClick -> resetCounts()
            MonitorAction.ScanClick -> startScan()
            is MonitorAction.ConnectClick -> connect(action.device)
            MonitorAction.DisconnectClick -> disconnect()
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

                    if (analysis.brakingCount > previous.brakingCount) outbox.trySend("BRAKING:${analysis.brakingCount}")
                    if (analysis.turnCount > previous.turnCount) outbox.trySend("TURN:${analysis.turnCount}")
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

    private fun startScan() {
        if (!_state.value.ble.canScan) return
        updateBle { it.copy(connection = ConnectionState.SCANNING, devices = emptyList()) }
        log("Scan started")
        viewModelScope.launch {
            val found = scanner.scan()
            updateBle { it.copy(connection = ConnectionState.DISCONNECTED, devices = found) }
            found.forEach { log("Device found: ${it.name} (${it.address})") }
        }
    }

    private fun connect(device: BleDevice) {
        if (!_state.value.ble.canConnect) return
        updateBle { it.copy(connection = ConnectionState.CONNECTING) }
        log("Connecting to ${device.name}...")
        connectJob = viewModelScope.launch {
            connector.connect(device)
            updateBle { it.copy(connection = ConnectionState.CONNECTED, connectedDevice = device) }
            log("Connected to ${device.name}; service ${BleProfile.SERVICE_DRIVE_HUB}")
        }
    }

    private fun disconnect() {
        if (!_state.value.ble.canDisconnect) return
        connectJob?.cancel()
        connector.disconnect()
        updateBle { it.copy(connection = ConnectionState.DISCONNECTED, connectedDevice = null) }
        log("Disconnected")
    }

    private suspend fun transmit(packet: String) {
        if (!_state.value.ble.isLinked) {
            log("Not sent (not connected): $packet")
            return
        }
        updateBle { it.copy(connection = ConnectionState.DATA_TRANSFER) }
        log("TX -> ${BleProfile.CHAR_COMMAND}: $packet")
        try {
            val reply = connector.sendData(packet)
            log("ACK <- ${BleProfile.CHAR_STATUS}: $reply")
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalStateException) {
            log("Send failed: ${e.message}")
        }

        updateBle { if (it.connection == ConnectionState.DATA_TRANSFER) it.copy(connection = ConnectionState.CONNECTED) else it }
    }

    private fun updateBle(transform: (BleState) -> BleState) = _state.update { it.copy(ble = transform(it.ble)) }

    private fun log(message: String) {
        logger.d(message)
        updateBle { it.copy(log = (it.log + LogEntry(System.currentTimeMillis(), message)).takeLast(MAX_LOG_ENTRIES)) }
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 100
    }
}
