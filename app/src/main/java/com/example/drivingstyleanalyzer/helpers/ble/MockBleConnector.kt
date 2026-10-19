package com.example.drivingstyleanalyzer.helpers.ble

import kotlinx.coroutines.delay

class MockBleConnector(
    private val connectDelayMs: Long = 1_000L,
    private val transferDelayMs: Long = 300L
) : IBleConnector {
    @Volatile
    private var connected: BleDevice? = null

    override suspend fun connect(device: BleDevice) {
        delay(connectDelayMs)
        connected = device
    }

    override fun disconnect() {
        connected = null
    }

    override suspend fun sendData(data: String): String {
        check(connected != null) { "Not connected" }
        delay(transferDelayMs)
        check(connected != null) { "Link lost during transfer" }
        return "OK"
    }
}
