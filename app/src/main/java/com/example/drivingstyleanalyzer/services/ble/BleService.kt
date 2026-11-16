package com.example.drivingstyleanalyzer.services.ble

import com.example.drivingstyleanalyzer.integrations.ble.BleDevice
import com.example.drivingstyleanalyzer.integrations.ble.BleLogger
import com.example.drivingstyleanalyzer.integrations.ble.BleProfile
import com.example.drivingstyleanalyzer.integrations.ble.ConnectionState
import com.example.drivingstyleanalyzer.integrations.ble.IBleConnector
import com.example.drivingstyleanalyzer.integrations.ble.MockBleConnector
import com.example.drivingstyleanalyzer.integrations.ble.MockBleScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleService(private val scope: CoroutineScope) {
    private val scanner = MockBleScanner()
    private val connector: IBleConnector = MockBleConnector()
    private val logger = BleLogger()
    private val _state = MutableStateFlow(BleState())
    val state = _state.asStateFlow()

    private var connectJob: Job? = null
    private var linkGeneration = 0
    private val outbox = Channel<String>(Channel.UNLIMITED)

    init {
        scope.launch { for (packet in outbox) transmit(packet) }
    }

    fun send(packet: String) {
        outbox.trySend(packet)
    }

    fun scan() {
        if (!_state.value.canScan) return
        updateBle { it.copy(connection = ConnectionState.SCANNING, devices = emptyList()) }
        log("Scan started")
        scope.launch {
            val found = scanner.scan()
            updateBle { it.copy(connection = ConnectionState.DISCONNECTED, devices = found) }
            found.forEach { log("Device found: ${it.name} (${it.address})") }
        }
    }

    fun connect(device: BleDevice) {
        if (!_state.value.canConnect) return
        updateBle { it.copy(connection = ConnectionState.CONNECTING) }
        log("Connecting to ${device.name}...")
        connectJob = scope.launch {
            connector.connect(device)
            updateBle { it.copy(connection = ConnectionState.CONNECTED, connectedDevice = device) }
            log("Connected to ${device.name}; service ${BleProfile.SERVICE_DRIVE_HUB}")
        }
    }

    fun disconnect() {
        if (!_state.value.canDisconnect) return
        linkGeneration++
        connectJob?.cancel()
        connector.disconnect()
        updateBle { it.copy(connection = ConnectionState.DISCONNECTED, connectedDevice = null) }
        log("Disconnected")
    }

    private suspend fun transmit(packet: String) {
        if (!_state.value.isLinked) {
            log("Not sent (not connected): $packet")
            return
        }
        val generation = linkGeneration
        updateBle { it.copy(connection = ConnectionState.DATA_TRANSFER) }
        log("TX -> ${BleProfile.CHAR_COMMAND}: $packet")
        try {
            val reply = connector.sendData(packet)
            if (generation == linkGeneration) log("ACK <- ${BleProfile.CHAR_STATUS}: $reply")
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalStateException) {
            log("Send failed: ${e.message}")
        }

        updateBle { if (generation == linkGeneration && it.connection == ConnectionState.DATA_TRANSFER) it.copy(connection = ConnectionState.CONNECTED) else it }
    }

    private fun updateBle(transform: (BleState) -> BleState) = _state.update(transform)

    private fun log(message: String) {
        logger.d(message)
        updateBle { it.copy(log = (it.log + LogEntry(System.currentTimeMillis(), message)).takeLast(MAX_LOG_ENTRIES)) }
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 100
    }
}
