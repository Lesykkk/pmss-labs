package com.example.drivingstyleanalyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF245D91),
    secondary = Color(0xFF46697B),
    tertiary = Color(0xFF466B45)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF99CBFF),
    secondary = Color(0xFFACCCDF),
    tertiary = Color(0xFFACD5A6)
)

@Composable
fun DrivingStyleAnalyzerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
