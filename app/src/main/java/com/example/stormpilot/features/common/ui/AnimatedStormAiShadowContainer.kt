package com.example.stormpilot.features.common.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

internal val StormAiShadowColorInts = intArrayOf(
    android.graphics.Color.parseColor("#00FF88"), // mint green
    android.graphics.Color.parseColor("#00CCAA"), // teal
    android.graphics.Color.parseColor("#0088FF"), // sky blue
    android.graphics.Color.parseColor("#88FF00"), // lime
    android.graphics.Color.parseColor("#00FF88"), // mint green (loop back)
)

internal val StormAiShadowColors = listOf(
    Color(0xFF00FF88), // mint green
    Color(0xFF00CCAA), // teal
    Color(0xFF0088FF), // sky blue
    Color(0xFF88FF00), // lime
)

@Composable
@Suppress("DEPRECATION")
fun AnimatedStormAiShadowContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp? = null,
    blurRadius: Dp = 5.dp,
    shadowPadding: Dp = 4.dp,
    drawBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stormAiAnimatedShadow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stormAiAnimatedShadowAngle",
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(shadowPadding)
                .drawWithCache {
                    onDrawBehind {
                        if (size.width == 0f || size.height == 0f) return@onDrawBehind

                        val shader = android.graphics.SweepGradient(
                            size.width / 2f,
                            size.height / 2f,
                            StormAiShadowColorInts,
                            null,
                        )
                        val matrix = android.graphics.Matrix()
                        matrix.setRotate(angle, size.width / 2f, size.height / 2f)
                        shader.setLocalMatrix(matrix)

                        val shadowPaint = Paint().apply {
                            isAntiAlias = true
                            this.shader = shader
                        }.apply {
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
                                blurRadius.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL,
                            )
                        }

                        val borderPaint = Paint().apply {
                            isAntiAlias = true
                            this.shader = shader
                        }
                        val radius = cornerRadius
                            ?.toPx()
                            ?.coerceAtMost(min(size.width, size.height) / 2f)
                            ?: size.height / 2f
                        val rect = Rect(0f, 0f, size.width, size.height)

                        drawIntoCanvas { canvas ->
                            canvas.drawRoundRect(
                                left = rect.left,
                                top = rect.top,
                                right = rect.right,
                                bottom = rect.bottom,
                                radiusX = radius,
                                radiusY = radius,
                                paint = shadowPaint,
                            )

                            if (drawBorder) {
                                canvas.drawRoundRect(
                                    left = rect.left,
                                    top = rect.top,
                                    right = rect.right,
                                    bottom = rect.bottom,
                                    radiusX = radius,
                                    radiusY = radius,
                                    paint = borderPaint,
                                )
                            }
                        }
                    }
                },
        )

        content()
    }
}
