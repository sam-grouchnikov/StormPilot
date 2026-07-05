package com.example.stormpilot.features.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stormpilot.ui.theme.extendedColors

@Composable
@Suppress("DEPRECATION")
fun DimmedBackdrop(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (visible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.extendedColors.stormAi.dimmedBackground.copy(alpha = 0.6f),
                ),
        )
    }
}
