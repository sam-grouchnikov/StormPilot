package com.example.stormpilot.core

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppSettings {
    var mode by mutableStateOf("system")
    var isDarkMode by mutableStateOf(true)

    var avoidStorms by mutableStateOf(true)

    val dashboardPlaceholderWeatherFiles = listOf(
        "dashboard_weather_placeholder.json",
        "dashboard_weather_placeholder.txt",
    )
    var useDashboardPlaceholderData by mutableStateOf(true)
    var dashboardPlaceholderWeatherAssetName by mutableStateOf(dashboardPlaceholderWeatherFiles.first())

    fun cycleDashboardPlaceholderWeatherFile() {
        val currentIndex = dashboardPlaceholderWeatherFiles
            .indexOf(dashboardPlaceholderWeatherAssetName)
            .takeIf { it >= 0 }
            ?: 0
        dashboardPlaceholderWeatherAssetName =
            dashboardPlaceholderWeatherFiles[(currentIndex + 1) % dashboardPlaceholderWeatherFiles.size]
    }
}

@Composable
fun isAppInDarkMode(): Boolean {
    return when (AppSettings.mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun isAppInAvoidStormMode(): Boolean {
    return AppSettings.avoidStorms
}

fun updateAppCompatNightMode(mode: String) {
    val nightMode = when (mode) {
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(nightMode)
}
