package com.example.drivingstyleanalyzer.monitor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.Acceleration
import kotlin.math.min

@Composable
fun AccelerationIndicator(acceleration: Acceleration) {
    val colors = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.indicator_title), style = MaterialTheme.typography.titleMedium)
            Canvas(Modifier.fillMaxWidth().height(190.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = min(size.width, size.height) / 2f - 16.dp.toPx()
                val grid = colors.onSurfaceVariant.copy(alpha = 0.3f)
                drawCircle(grid, radius, center, style = Stroke(1.dp.toPx()))
                drawCircle(grid, radius / 2f, center, style = Stroke(1.dp.toPx()))
                drawLine(grid, center - Offset(radius, 0f), center + Offset(radius, 0f))
                drawLine(grid, center - Offset(0f, radius), center + Offset(0f, radius))
                val position = center + Offset(
                    acceleration.x.coerceIn(-MAX_ACCELERATION, MAX_ACCELERATION) / MAX_ACCELERATION * radius,
                    -acceleration.y.coerceIn(-MAX_ACCELERATION, MAX_ACCELERATION) / MAX_ACCELERATION * radius
                )
                drawLine(colors.primary, center, position, strokeWidth = 2.dp.toPx())
                drawCircle(colors.primary.copy(alpha = 0.15f), 18.dp.toPx(), position)
                drawCircle(colors.primary, 8.dp.toPx(), position)
            }
            Text(stringResource(R.string.indicator_caption), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private const val MAX_ACCELERATION = 10f
