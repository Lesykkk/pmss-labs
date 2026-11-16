package com.example.drivingstyleanalyzer.integrations.firebase

import com.example.drivingstyleanalyzer.models.TelemetryRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirebaseTelemetryClient(database: FirebaseDatabase) {
    private val connection = database.getReference(".info/connected")
    private val records = database.getReference("sensor_data")

    val connected = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        connection.addValueEventListener(listener)
        awaitClose { connection.removeEventListener(listener) }
    }

    fun write(deviceId: String, record: TelemetryRecord, onComplete: (Result<Unit>) -> Unit) {
        val payload = mapOf(
            "deviceId" to deviceId,
            "sensorType" to "accelerometer",
            "type" to record.type.name,
            "timestamp" to record.timestampMs,
            "x" to record.acceleration.x,
            "y" to record.acceleration.y,
            "z" to record.acceleration.z
        )
        records.push().setValue(payload) { error, _ ->
            onComplete(if (error == null) Result.success(Unit) else Result.failure(error.toException()))
        }
    }
}
