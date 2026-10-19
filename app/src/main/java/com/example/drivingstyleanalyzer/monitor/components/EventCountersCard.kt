package com.example.drivingstyleanalyzer.monitor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.DrivingAnalysis

@Composable
fun EventCountersCard(analysis: DrivingAnalysis, onReset: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.event_counters), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                EventCounter(stringResource(R.string.braking_label), analysis.brakingCount, Modifier.weight(1f))
                EventCounter(stringResource(R.string.turn_label), analysis.turnCount, Modifier.weight(1f))
            }
            OutlinedButton(onClick = onReset) { Text(stringResource(R.string.reset_counts)) }
        }
    }
}

@Composable
private fun EventCounter(label: String, count: Int, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
    }
}
