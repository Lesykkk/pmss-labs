package com.example.drivingstyleanalyzer.repositories.telemetry

enum class SyncStatus { IDLE, SYNCING, PENDING, SYNCED, FAILED }

data class TelemetrySync(
    val connected: Boolean = false,
    val pendingCount: Int = 0,
    val confirmedCount: Int = 0,
    val lastWriteFailed: Boolean = false
) {
    val status: SyncStatus
        get() = when {
            pendingCount > 0 && !connected -> SyncStatus.PENDING
            lastWriteFailed -> SyncStatus.FAILED
            pendingCount > 0 -> SyncStatus.SYNCING
            confirmedCount > 0 -> SyncStatus.SYNCED
            else -> SyncStatus.IDLE
        }
}
