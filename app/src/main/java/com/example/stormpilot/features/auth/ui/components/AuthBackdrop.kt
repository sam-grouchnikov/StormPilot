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
            val broadRainWidth = 18.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.18f),
                        scheme.tertiary.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.76f, height * 0.18f),
                    radius = width * 0.72f,
                ),
                radius = width * 0.72f,
                center = Offset(width * 0.76f, height * 0.18f),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scheme.secondary.copy(alpha = 0.14f),
                        scheme.primaryContainer.copy(alpha = 0.09f),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.18f, height * 0.84f),
                    radius = width * 0.64f,
                ),
                radius = width * 0.64f,
                center = Offset(width * 0.18f, height * 0.84f),
            )

            rotate(degrees = -14f, pivot = Offset(width / 2f, height / 2f)) {
                repeat(4) { index ->
                    val y = height * (0.16f + index * 0.23f)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.primary.copy(alpha = 0.08f),
                                scheme.tertiary.copy(alpha = 0.05f),
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

            repeat(18) { index ->
                val spacing = sweepDistance / 7f
                val startX = ((index * spacing * 0.68f) % (width + spacing * 2f)) - spacing
                val startY = -height * 0.12f + (index % 6) * height * 0.18f
                drawLine(
                    color = scheme.primary.copy(alpha = if (index % 3 == 0) 0.18f else 0.11f),
                    start = Offset(startX, startY),
                    end = Offset(startX - width * 0.32f, startY + height * 0.72f),
                    strokeWidth = fineRainWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
