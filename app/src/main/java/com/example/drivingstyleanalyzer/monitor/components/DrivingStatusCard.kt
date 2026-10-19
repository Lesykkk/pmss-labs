package com.example.drivingstyleanalyzer.monitor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.StatusKind

@Composable
fun DrivingStatusCard(kind: StatusKind, isWarning: Boolean) {
    @StringRes val message = when (kind) {
        StatusKind.WAITING -> R.string.status_waiting
        StatusKind.PAUSED -> R.string.status_paused
        StatusKind.MISSING -> R.string.status_missing
        StatusKind.ERROR -> R.string.status_error
        StatusKind.SMOOTH -> R.string.status_smooth
        StatusKind.BRAKING -> R.string.status_braking
        StatusKind.TURN -> R.string.status_turn
        StatusKind.BOTH -> R.string.status_both
    }
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) colors.errorContainer else colors.primaryContainer,
            contentColor = if (isWarning) colors.onErrorContainer else colors.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.current_status), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(message), style = MaterialTheme.typography.titleLarge)
        }
    }
}
