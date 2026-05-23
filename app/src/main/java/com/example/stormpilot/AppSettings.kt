package com.example.stormpilot

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppSettings {
    var mode by mutableStateOf("system")
    var isDarkMode by mutableStateOf(true)
}

@Composable
fun isAppInDarkMode(): Boolean {
    return when (AppSettings.mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
}

fun updateAppCompatNightMode(mode: String) {
    val nightMode = when (mode) {
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(nightMode)
}
