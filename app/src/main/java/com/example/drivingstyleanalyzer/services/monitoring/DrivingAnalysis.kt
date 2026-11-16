package com.example.drivingstyleanalyzer.services.monitoring

data class DrivingAnalysis(
    val isBraking: Boolean = false,
    val isTurning: Boolean = false,
    val brakingCount: Int = 0,
    val turnCount: Int = 0
)
