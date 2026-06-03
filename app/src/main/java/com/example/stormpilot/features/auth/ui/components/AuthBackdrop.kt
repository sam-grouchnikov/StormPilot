package com.example.stormpilot.features.auth.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AnimatedStormBackdrop() {
    val scheme = MaterialTheme.colorScheme
    val drift = rememberInfiniteTransition(label = "storm_backdrop")
    val xShift by drift.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "storm_backdrop_x",
    )
    val yShift by drift.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "storm_backdrop_y",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offsetFromAnimation(x = xShift, y = yShift)
                .size(240.dp)
                .blur(18.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = 0.24f),
                            scheme.tertiary.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        center = Offset(140f, 140f),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offsetFromAnimation(x = -xShift * 0.6f, y = -yShift * 0.45f)
                .size(220.dp)
                .blur(24.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.secondary.copy(alpha = 0.18f),
                            scheme.primaryContainer.copy(alpha = 0.14f),
                            Color.Transparent,
                        ),
                        center = Offset(120f, 120f),
                    ),
                ),
        )
    }
}

@Composable
fun Modifier.signInEntrance(visible: Boolean, index: Int): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing,
        ),
        label = "signin_alpha_$index",
    )
    val y by animateFloatAsState(
        targetValue = if (visible) 0f else 28f,
        animationSpec = tween(
            durationMillis = 560,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing,
        ),
        label = "signin_y_$index",
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = y
    }
}

@Composable
private fun Modifier.offsetFromAnimation(x: Float, y: Float): Modifier {
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(x.roundToInt(), y.roundToInt()),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "storm_offset",
    )
    return then(
        Modifier.graphicsLayer {
            translationX = animatedOffset.x.toFloat()
            translationY = animatedOffset.y.toFloat()
        },
    )
}
