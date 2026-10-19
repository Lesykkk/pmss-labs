package com.example.drivingstyleanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.drivingstyleanalyzer.monitor.MonitorAction
import com.example.drivingstyleanalyzer.monitor.MonitorScreen
import com.example.drivingstyleanalyzer.monitor.MonitorViewModel
import com.example.drivingstyleanalyzer.ui.theme.DrivingStyleAnalyzerTheme

class MainActivity : ComponentActivity() {
    private val vm: MonitorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrivingStyleAnalyzerTheme {
                MonitorScreen(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onAction(MonitorAction.Start)
    }

    override fun onPause() {
        vm.onAction(MonitorAction.Stop)
        super.onPause()
    }
}
