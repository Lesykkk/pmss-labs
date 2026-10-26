package com.example.drivingstyleanalyzer.monitor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivingstyleanalyzer.R
import com.example.drivingstyleanalyzer.monitor.components.AccelerationIndicator
import com.example.drivingstyleanalyzer.monitor.components.BleControlCard
import com.example.drivingstyleanalyzer.monitor.components.BleLogCard
import com.example.drivingstyleanalyzer.monitor.components.CloudSyncCard
import com.example.drivingstyleanalyzer.monitor.components.DrivingStatusCard
import com.example.drivingstyleanalyzer.monitor.components.EventCountersCard
import com.example.drivingstyleanalyzer.monitor.components.SensorReadingsCard
import com.example.drivingstyleanalyzer.ui.theme.DrivingStyleAnalyzerTheme

@Composable
fun MonitorScreen(vm: MonitorViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val countersResetMessage = stringResource(R.string.counters_reset)
    val noNetworkMessage = stringResource(R.string.no_network_message)

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                MonitorEvent.CountersReset ->
                    Toast.makeText(context, countersResetMessage, Toast.LENGTH_SHORT).show()

                MonitorEvent.NoNetwork ->
                    Toast.makeText(context, noNetworkMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    MonitorContent(state = state, onAction = vm::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorContent(state: MonitorState, onAction: (MonitorAction) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DrivingStatusCard(state.statusKind, state.isWarning) }
            item { SensorReadingsCard(state.acceleration) }
            item { AccelerationIndicator(state.acceleration) }
            item { EventCountersCard(state.analysis, onReset = { onAction(MonitorAction.ResetClick) }) }
            item { CloudSyncCard(state.cloud) }
            item { BleControlCard(state.ble, onAction) }
            item { BleLogCard(state.ble.log) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonitorPreview() {
    DrivingStyleAnalyzerTheme {
        MonitorContent(state = MonitorState(), onAction = {})
    }
}
