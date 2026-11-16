package com.example.drivingstyleanalyzer.repositories.telemetry

import android.util.Log
import com.example.drivingstyleanalyzer.integrations.firebase.FirebaseTelemetryClient
import com.example.drivingstyleanalyzer.models.TelemetryRecord
import com.example.drivingstyleanalyzer.models.TelemetryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TelemetryRepository(
    private val client: FirebaseTelemetryClient,
    private val deviceId: String,
    scope: CoroutineScope
) {
    private val _sync = MutableStateFlow(TelemetrySync())
    val sync = _sync.asStateFlow()
    private var lastSampleNs: Long? = null

    init {
        scope.launch {
            client.connected
                .catch { error ->
                    Log.w(TAG, "Could not observe Firebase connection", error)
                    _sync.update { it.copy(lastWriteFailed = true) }
                }
                .collect { connected -> _sync.update { it.copy(connected = connected) } }
        }
    }

    fun save(record: TelemetryRecord, sampleTimeNs: Long) {
        if (record.type == TelemetryType.ACCELERATION) {
            val previous = lastSampleNs
            if (previous != null && sampleTimeNs - previous < SAMPLE_INTERVAL_NS) return
            lastSampleNs = sampleTimeNs
        }
        _sync.update { it.copy(pendingCount = it.pendingCount + 1) }
        client.write(deviceId, record) { result ->
            result.exceptionOrNull()?.let { Log.w(TAG, "Could not write ${record.type}", it) }
            _sync.update {
                it.copy(
                    pendingCount = it.pendingCount - 1,
                    confirmedCount = it.confirmedCount + if (result.isSuccess) 1 else 0,
                    lastWriteFailed = result.isFailure
                )
            }
        }
    }

    private companion object {
        const val TAG = "TelemetryRepository"
        const val SAMPLE_INTERVAL_NS = 5_000_000_000L
    }
}
