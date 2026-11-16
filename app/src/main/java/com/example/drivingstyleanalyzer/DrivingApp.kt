package com.example.drivingstyleanalyzer

import android.app.Application
import com.example.drivingstyleanalyzer.integrations.firebase.FirebaseTelemetryClient
import com.example.drivingstyleanalyzer.repositories.telemetry.TelemetryRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

class DrivingApp : Application() {
    val telemetry by lazy {
        val preferences = getSharedPreferences("telemetry", MODE_PRIVATE)
        val deviceId = preferences.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            preferences.edit().putString("device_id", it).apply()
        }
        TelemetryRepository(
            client = FirebaseTelemetryClient(FirebaseDatabase.getInstance()),
            deviceId = deviceId,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
}
