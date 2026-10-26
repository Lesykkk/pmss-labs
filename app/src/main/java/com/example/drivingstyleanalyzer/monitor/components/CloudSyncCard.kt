package com.example.drivingstyleanalyzer.monitor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.CloudState
import com.example.drivingstyleanalyzer.monitor.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SyncedGreen = Color(0xFF2E9E4F)
private val PendingAmber = Color(0xFFF9A825)
private val FailedRed = Color(0xFFD32F2F)
private val IdleGray = Color(0xFF9E9E9E)

@Composable
fun CloudSyncCard(cloud: CloudState) {
    @StringRes val message = when (cloud.status) {
        SyncStatus.IDLE -> R.string.cloud_idle
        SyncStatus.SYNCED -> R.string.cloud_synced
        SyncStatus.PENDING -> R.string.cloud_pending
        SyncStatus.FAILED -> R.string.cloud_failed
    }
    val indicator = when (cloud.status) {
        SyncStatus.IDLE -> IdleGray
        SyncStatus.SYNCED -> SyncedGreen
        SyncStatus.PENDING -> PendingAmber
        SyncStatus.FAILED -> FailedRed
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.cloud_title), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(16.dp).clip(CircleShape).background(indicator))
                Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
            }
            Text(stringResource(R.string.cloud_stats, cloud.sentCount), style = MaterialTheme.typography.bodyMedium)
            cloud.lastSyncMs?.let {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
                Text(stringResource(R.string.cloud_last_sync, time), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
