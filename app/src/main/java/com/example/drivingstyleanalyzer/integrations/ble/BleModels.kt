package com.example.drivingstyleanalyzer.integrations.ble

data class BleDevice(val name: String, val address: String)

enum class ConnectionState { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, DATA_TRANSFER }
