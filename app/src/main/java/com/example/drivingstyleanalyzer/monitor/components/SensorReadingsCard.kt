package com.example.drivingstyleanalyzer.monitor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.Acceleration
import java.util.Locale

@Composable
fun SensorReadingsCard(acceleration: Acceleration) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.accelerometer_readings), style = MaterialTheme.typography.titleMedium)
            AxisReadings(acceleration)
            Text(stringResource(R.string.acceleration_unit), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AxisReadings(value: Acceleration) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AxisValue("X", value.x, Modifier.weight(1f))
        AxisValue("Y", value.y, Modifier.weight(1f))
        AxisValue("Z", value.z, Modifier.weight(1f))
    }
}

@Composable
private fun AxisValue(axis: String, value: Float, modifier: Modifier) {
    Column(modifier) {
        Text(axis, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(
            String.format(Locale.getDefault(), "%+.2f", value),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
