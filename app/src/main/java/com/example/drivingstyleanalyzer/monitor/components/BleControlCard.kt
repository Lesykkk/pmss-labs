package com.example.drivingstyleanalyzer.monitor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.helpers.ble.ConnectionState
import com.example.drivingstyleanalyzer.monitor.BleState
import com.example.drivingstyleanalyzer.monitor.MonitorAction

@Composable
fun BleControlCard(ble: BleState, onAction: (MonitorAction) -> Unit) {
    @StringRes val statusText = when (ble.connection) {
        ConnectionState.DISCONNECTED -> R.string.ble_disconnected
        ConnectionState.SCANNING -> R.string.ble_scanning
        ConnectionState.CONNECTING -> R.string.ble_connecting
        ConnectionState.CONNECTED -> R.string.ble_connected
        ConnectionState.DATA_TRANSFER -> R.string.ble_data_transfer
    }
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (ble.isLinked) colors.tertiaryContainer else colors.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.ble_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.ble_status, stringResource(statusText)), style = MaterialTheme.typography.bodyLarge)

            if (ble.showProgress) LinearProgressIndicator(Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onAction(MonitorAction.ScanClick) }, enabled = ble.canScan) {
                    Text(stringResource(R.string.ble_scan))
                }
                OutlinedButton(onClick = { onAction(MonitorAction.DisconnectClick) }, enabled = ble.canDisconnect) {
                    Text(stringResource(R.string.ble_disconnect))
                }
            }

            Text(stringResource(R.string.ble_devices), style = MaterialTheme.typography.titleSmall)
            if (ble.devices.isEmpty()) {
                Text(stringResource(R.string.ble_no_devices), style = MaterialTheme.typography.bodyMedium)
            }
            ble.devices.forEach { device ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(device.name, style = MaterialTheme.typography.bodyLarge)
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onAction(MonitorAction.ConnectClick(device)) }, enabled = ble.canConnect) {
                        Text(stringResource(R.string.ble_connect))
                    }
                }
            }
        }
    }
}
