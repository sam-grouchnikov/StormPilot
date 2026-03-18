package com.example.stormpilot.pages.subnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.compose.StormPilotTheme

@Composable fun RadarPage() {
    var isDarkMode by remember { mutableStateOf(false) }

    StormPilotTheme(darkTheme = isDarkMode) {

    }
}
