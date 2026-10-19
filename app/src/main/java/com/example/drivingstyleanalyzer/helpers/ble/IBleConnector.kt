package com.example.drivingstyleanalyzer.helpers.ble

interface IBleConnector {
    suspend fun connect(device: BleDevice)

    fun disconnect()

    suspend fun sendData(data: String): String
}
