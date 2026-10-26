package com.example.drivingstyleanalyzer.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class SensorPayload(
    val deviceId: String,
    val sensorType: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long
)

interface CloudApi {
    @POST("posts")
    suspend fun upload(@Body payload: SensorPayload)

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): CloudApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApi::class.java)
    }
}
