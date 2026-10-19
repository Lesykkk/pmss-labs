package com.example.drivingstyleanalyzer.helpers.ble

import java.util.UUID

object BleProfile {
    const val DEVICE_NAME = "DriveHub-Mock"
    const val DEVICE_ADDRESS = "00:11:22:33:44:55"

    val SERVICE_DRIVE_HUB: UUID = UUID.fromString("0000d001-0000-1000-8000-00805f9b34fb")

    val CHAR_STATUS: UUID = UUID.fromString("0000d002-0000-1000-8000-00805f9b34fb")

    val CHAR_COMMAND: UUID = UUID.fromString("0000d003-0000-1000-8000-00805f9b34fb")
}
