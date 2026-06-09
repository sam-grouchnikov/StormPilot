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
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val StormAiShadowColors = intArrayOf(
    android.graphics.Color.parseColor("#4A90E2"), // blue
    android.graphics.Color.parseColor("#7B4FD6"), // purple
    android.graphics.Color.parseColor("#E040C8"), // magenta
    android.graphics.Color.parseColor("#4A90E2"), // blue
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
                            StormAiShadowColors,
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
