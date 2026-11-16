package com.example.drivingstyleanalyzer.ui.monitor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.repositories.telemetry.SyncStatus
import com.example.drivingstyleanalyzer.repositories.telemetry.TelemetrySync

@Composable
fun CloudSyncCard(sync: TelemetrySync) {
    val (label, color) = when (sync.status) {
        SyncStatus.IDLE -> R.string.cloud_idle to MaterialTheme.colorScheme.onSurfaceVariant
        SyncStatus.SYNCING -> R.string.cloud_syncing to MaterialTheme.colorScheme.onSurfaceVariant
        SyncStatus.PENDING -> R.string.cloud_pending to Color(0xFF9A5900)
        SyncStatus.SYNCED -> R.string.cloud_synced to Color(0xFF24733C)
        SyncStatus.FAILED -> R.string.cloud_failed to MaterialTheme.colorScheme.error
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.cloud_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(label), color = color)
            Text(stringResource(R.string.cloud_confirmed, sync.confirmedCount))
        }
    }
}
