package com.example.drivingstyleanalyzer.monitor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val VISIBLE_LINES = 15

@Composable
fun BleLogCard(log: List<LogEntry>) {
    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.ble_log_title), style = MaterialTheme.typography.titleMedium)
            if (log.isEmpty()) {
                Text(stringResource(R.string.ble_log_empty), style = MaterialTheme.typography.bodyMedium)
            }
            log.takeLast(VISIBLE_LINES).asReversed().forEach { entry ->
                Text(
                    "${format.format(Date(entry.timestampMs))}  ${entry.message}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
