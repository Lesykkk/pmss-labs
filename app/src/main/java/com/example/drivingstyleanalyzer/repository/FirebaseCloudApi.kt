package com.example.drivingstyleanalyzer.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseCloudApi(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : CloudApi {

    override suspend fun upload(payload: SensorPayload) {
        database.getReference(NODE).push().setValue(payload).await()
    }

    private companion object {
        const val NODE = "sensor_data"
    }
}
