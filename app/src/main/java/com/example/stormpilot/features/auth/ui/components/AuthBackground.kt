package com.example.stormpilot.features.auth.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import com.example.stormpilot.ui.theme.ExtendedAuthColors

internal fun Modifier.authBackgroundGradient(colors: ExtendedAuthColors): Modifier = drawWithCache {
    // Place the glow just below the top edge and fade it out in the upper third.
    val radius = minOf(size.width * 0.61f, size.height * 0.29f)
    val center = Offset(size.width / 2f, radius * 0.065f)
    val gradient = Brush.radialGradient(
        colors = listOf(colors.backgroundStart, colors.backgroundEnd),
        center = center,
        radius = radius,
    )

    onDrawBehind {
        scale(scaleX = 1.2f, scaleY = 0.9f, pivot = center) {
            drawCircle(brush = gradient, radius = radius, center = center)
        }
    }
}
