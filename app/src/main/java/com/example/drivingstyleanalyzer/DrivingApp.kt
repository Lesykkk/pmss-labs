package com.example.drivingstyleanalyzer

import android.app.Application
import com.example.drivingstyleanalyzer.repository.CloudApi
import com.example.drivingstyleanalyzer.repository.SensorRepository

class DrivingApp : Application() {
    val sensorRepository: SensorRepository by lazy { SensorRepository(CloudApi.create()) }
}
