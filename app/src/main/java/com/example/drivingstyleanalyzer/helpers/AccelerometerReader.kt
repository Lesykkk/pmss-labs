package com.example.drivingstyleanalyzer.helpers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class AccelerometerSample(val x: Float, val y: Float, val z: Float, val timestampNs: Long)

class AccelerometerReader(context: Context) {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean get() = sensor != null

    fun readings(): Flow<AccelerometerSample> = callbackFlow {
        val sensorManager = manager
        val accelerometer = sensor
        if (sensorManager == null || accelerometer == null) {
            close(IllegalStateException("Accelerometer is not available"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values.size < 3) return
                val (x, y, z) = event.values
                if (!x.isFinite() || !y.isFinite() || !z.isFinite()) return
                trySend(AccelerometerSample(x, y, z, event.timestamp))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (!sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
            close(IllegalStateException("Failed to register the accelerometer listener"))
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
