package com.example.drivingstyleanalyzer.monitor

import com.example.drivingstyleanalyzer.helpers.ble.BleDevice
import com.example.drivingstyleanalyzer.helpers.ble.BleLogger
import com.example.drivingstyleanalyzer.helpers.ble.BleProfile
import com.example.drivingstyleanalyzer.helpers.ble.ConnectionState
import com.example.drivingstyleanalyzer.helpers.ble.IBleConnector
import com.example.drivingstyleanalyzer.helpers.ble.MockBleConnector
import com.example.drivingstyleanalyzer.helpers.ble.MockBleScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class BleController(
    private val scope: CoroutineScope,
    private val current: () -> BleState,
    private val update: ((BleState) -> BleState) -> Unit,
    private val scanner: MockBleScanner = MockBleScanner(),
    private val connector: IBleConnector = MockBleConnector(),
    private val logger: BleLogger = BleLogger()
) {
    private var connectJob: Job? = null
    private val outbox = Channel<String>(Channel.UNLIMITED)

    init {
        scope.launch { for (packet in outbox) transmit(packet) }
    }

    fun send(packet: String) {
        outbox.trySend(packet)
    }

    fun scan() {
        if (!current().canScan) return
        update { it.copy(connection = ConnectionState.SCANNING, devices = emptyList()) }
        log("Scan started")
        scope.launch {
            val found = scanner.scan()
            update { it.copy(connection = ConnectionState.DISCONNECTED, devices = found) }
            found.forEach { log("Device found: ${it.name} (${it.address})") }
        }
    }

    fun connect(device: BleDevice) {
        if (!current().canConnect) return
        update { it.copy(connection = ConnectionState.CONNECTING) }
        log("Connecting to ${device.name}...")
        connectJob = scope.launch {
            connector.connect(device)
            update { it.copy(connection = ConnectionState.CONNECTED, connectedDevice = device) }
            log("Connected to ${device.name}; service ${BleProfile.SERVICE_DRIVE_HUB}")
        }
    }

    fun disconnect() {
        if (!current().canDisconnect) return
        connectJob?.cancel()
        connector.disconnect()
        update { it.copy(connection = ConnectionState.DISCONNECTED, connectedDevice = null) }
        log("Disconnected")
    }

    private suspend fun transmit(packet: String) {
        if (!current().isLinked) {
            log("Not sent (not connected): $packet")
            return
        }
        update { it.copy(connection = ConnectionState.DATA_TRANSFER) }
        log("TX -> ${BleProfile.CHAR_COMMAND}: $packet")
        try {
            val reply = connector.sendData(packet)
            log("ACK <- ${BleProfile.CHAR_STATUS}: $reply")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log("Send failed: ${e.message}")
        }

        update { if (it.connection == ConnectionState.DATA_TRANSFER) it.copy(connection = ConnectionState.CONNECTED) else it }
    }

    private fun log(message: String) {
        logger.d(message)
        update { it.copy(log = (it.log + LogEntry(System.currentTimeMillis(), message)).takeLast(MAX_LOG_ENTRIES)) }
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 100
    }
}
