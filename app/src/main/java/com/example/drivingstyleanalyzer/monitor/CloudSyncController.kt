package com.example.drivingstyleanalyzer.monitor

import com.example.drivingstyleanalyzer.repository.SensorReading
import com.example.drivingstyleanalyzer.repository.SensorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class CloudSyncController(
    private val scope: CoroutineScope,
    private val repository: SensorRepository,
    private val current: () -> CloudState,
    private val update: ((CloudState) -> CloudState) -> Unit,
    private val notify: suspend (MonitorEvent) -> Unit,
    private val throttle: UploadThrottle = UploadThrottle()
) {
    private var uploadJob: Job? = null

    fun onSample(acceleration: Acceleration, timestampNs: Long) {
        if (!throttle.shouldSend(acceleration, timestampNs / NANOS_PER_MILLI)) return
        upload(SensorReading(SENSOR_TYPE, acceleration.x, acceleration.y, acceleration.z, System.currentTimeMillis()))
    }

    private fun upload(reading: SensorReading) {
        if (uploadJob?.isActive == true) return
        uploadJob = scope.launch {
            repository.save(reading)
                .onSuccess {
                    update {
                        CloudState(
                            status = SyncStatus.SYNCED,
                            sentCount = it.sentCount + 1,
                            lastSyncMs = System.currentTimeMillis()
                        )
                    }
                }
                .onFailure { error ->
                    val rejected = error !is IOException
                    val wasPending = current().status == SyncStatus.PENDING
                    update { it.copy(status = if (rejected) SyncStatus.FAILED else SyncStatus.PENDING) }
                    if (!rejected && !wasPending) notify(MonitorEvent.NoNetwork)
                }
        }
    }

    private companion object {
        const val SENSOR_TYPE = "accelerometer"
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
