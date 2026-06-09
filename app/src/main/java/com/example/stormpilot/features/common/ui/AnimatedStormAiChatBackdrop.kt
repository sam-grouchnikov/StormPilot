package com.example.stormpilot.features.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
@Suppress("DEPRECATION")
fun AnimatedStormAiChatBackdrop(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val animationSpeed = 0.82f

    val outlineProgress = remember { Animatable(0f) }
    val outlineAlpha = remember { Animatable(0f) }
    val ambientAlpha = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "stormAiChatBackdrop")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stormAiChatBackdropPhase",
    )

    LaunchedEffect(visible) {
        if (visible) {
            outlineProgress.snapTo(0f)
            outlineAlpha.snapTo(0f)
            ambientAlpha.snapTo(0f)

            launch {
                outlineAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = (220 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
                )
            }
            outlineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (880 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
            )
            launch {
                outlineAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = (620 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
                )
            }
            ambientAlpha.animateTo(
                targetValue = 0.35f,
                animationSpec = tween(durationMillis = (720 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
            )
        } else {
            launch {
                outlineAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = (180 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
                )
            }
            ambientAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (420 * animationSpeed).toInt(), easing = FastOutSlowInEasing),
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val ambient = ambientAlpha.value
        val outline = outlineAlpha.value
        val phaseRadians = Math.toRadians(phase.toDouble())
        val waveX = cos(phaseRadians).toFloat()
        val waveY = sin(phaseRadians).toFloat()
        val longSide = max(size.width, size.height)

        if (ambient > 0.01f) {
            drawRect(color = Color.Black.copy(alpha = 0.06f * ambient))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StormAiShadowColors[0].copy(alpha = 0.36f * ambient),
                        StormAiShadowColors[0].copy(alpha = 0.12f * ambient),
                        Color.Transparent,
                    ),
                    center = Offset(
                        x = size.width * (0.18f + 0.08f * waveX),
                        y = size.height * (0.18f + 0.07f * waveY),
                    ),
                    radius = longSide * 0.56f,
                ),
                radius = longSide * 0.56f,
                center = Offset(
                    x = size.width * (0.18f + 0.08f * waveX),
                    y = size.height * (0.18f + 0.07f * waveY),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StormAiShadowColors[1].copy(alpha = 0.40f * ambient),
                        StormAiShadowColors[1].copy(alpha = 0.15f * ambient),
                        Color.Transparent,
                    ),
                    center = Offset(
                        x = size.width * (0.84f - 0.09f * waveY),
                        y = size.height * (0.42f + 0.10f * waveX),
                    ),
                    radius = longSide * 0.64f,
                ),
                radius = longSide * 0.64f,
                center = Offset(
                    x = size.width * (0.84f - 0.09f * waveY),
                    y = size.height * (0.42f + 0.10f * waveX),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StormAiShadowColors[2].copy(alpha = 0.36f * ambient),
                        StormAiShadowColors[2].copy(alpha = 0.14f * ambient),
                        Color.Transparent,
                    ),
                    center = Offset(
                        x = size.width * (0.45f + 0.10f * waveY),
                        y = size.height * (0.92f - 0.08f * waveX),
                    ),
                    radius = longSide * 0.58f,
                ),
                radius = longSide * 0.58f,
                center = Offset(
                    x = size.width * (0.45f + 0.10f * waveY),
                    y = size.height * (0.92f - 0.08f * waveX),
                ),
            )
        }

        if (outline > 0.01f) {
            val inset = 0.dp.toPx()
            val radius = 36.dp.toPx()
            val rect = Rect(inset, inset, size.width - inset, size.height - inset)
            val revealTop = size.height * (1f - outlineProgress.value)
            val shader = android.graphics.SweepGradient(
                size.width / 2f,
                size.height / 2f,
                StormAiShadowColorInts,
                null,
            )
            val matrix = android.graphics.Matrix()
            matrix.setRotate(phase, size.width / 2f, size.height / 2f)
            shader.setLocalMatrix(matrix)

            val shadowPaint = Paint().apply {
                isAntiAlias = true
                this.shader = shader
            }.apply {
                asFrameworkPaint().style = android.graphics.Paint.Style.STROKE
                asFrameworkPaint().strokeWidth = 18.dp.toPx()
                asFrameworkPaint().alpha = (245 * outline).toInt()
                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
                    34.dp.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL,
                )
            }
            val borderPaint = Paint().apply {
                isAntiAlias = true
                this.shader = shader
            }.apply {
                asFrameworkPaint().style = android.graphics.Paint.Style.STROKE
                asFrameworkPaint().strokeWidth = 8.dp.toPx()
                asFrameworkPaint().alpha = (250 * outline).toInt()
            }

            clipRect(left = 0f, top = revealTop, right = size.width, bottom = size.height) {
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
    }
}
