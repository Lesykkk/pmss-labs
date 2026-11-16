package com.example.drivingstyleanalyzer.integrations.ble

import android.util.Log

class BleLogger(private val tag: String = "MockBle") {
    fun d(message: String) {
        Log.d(tag, message)
    }
}
