package com.example.stormpilot.features.auth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun StormBackdrop() {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val sweepDistance = width + height
            val fineRainWidth = 1.1.dp.toPx()
            val broadRainWidth = 12.dp.toPx()

            rotate(degrees = -14f, pivot = Offset(width / 2f, height / 2f)) {
                repeat(3) { index ->
                    val y = height * (0.18f + index * 0.28f)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.primary.copy(alpha = 0.055f),
                                scheme.tertiary.copy(alpha = 0.035f),
                                Color.Transparent,
                            ),
                        ),
                        start = Offset(-width * 0.22f, y),
                        end = Offset(width * 1.22f, y),
                        strokeWidth = broadRainWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }

            repeat(14) { index ->
                val spacing = sweepDistance / 8f
                val startX = ((index * spacing * 0.72f) % (width + spacing * 2f)) - spacing
                val startY = -height * 0.08f + (index % 7) * height * 0.16f
                drawLine(
                    color = scheme.primary.copy(alpha = if (index % 3 == 0) 0.12f else 0.07f),
                    start = Offset(startX, startY),
                    end = Offset(startX - width * 0.26f, startY + height * 0.58f),
                    strokeWidth = fineRainWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
