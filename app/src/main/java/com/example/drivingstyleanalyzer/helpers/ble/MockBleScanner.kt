package com.example.drivingstyleanalyzer.helpers.ble

import kotlinx.coroutines.delay

class MockBleScanner(private val scanDurationMs: Long = 2_000L) {
    suspend fun scan(): List<BleDevice> {
        delay(scanDurationMs)
        return listOf(BleDevice(BleProfile.DEVICE_NAME, BleProfile.DEVICE_ADDRESS))
    }
}
