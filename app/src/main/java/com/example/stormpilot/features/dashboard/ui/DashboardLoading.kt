package com.example.stormpilot.features.dashboard.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SHIMMER_CYCLE_MILLIS = 1650
private const val SHIMMER_SWEEP_MILLIS = 1100

internal data class SkeletonShimmer(
    private val elapsedMillis: State<Float>,
    private val delayMillis: Int = 0,
) {
    fun delayedBy(millis: Int): SkeletonShimmer = copy(delayMillis = delayMillis + millis)

    fun sweepProgress(): Float {
        val progress = ((elapsedMillis.value - delayMillis) / SHIMMER_SWEEP_MILLIS).coerceIn(0f, 1f)
        return progress * progress
    }
}

@Composable
internal fun rememberSkeletonShimmer(): SkeletonShimmer {
    val elapsedMillis = rememberInfiniteTransition(label = "Skeleton shimmer").animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_CYCLE_MILLIS.toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = SHIMMER_CYCLE_MILLIS, easing = LinearEasing)),
        label = "Skeleton cycle time",
    )
    return remember(elapsedMillis) { SkeletonShimmer(elapsedMillis) }
}

@Composable
internal fun SkeletonBlock(
    shimmer: SkeletonShimmer,
    modifier: Modifier,
    cornerRadius: Dp = 6.dp,
) {
    val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val base = if (darkTheme) Color.White.copy(alpha = 0.19f) else Color.Black.copy(alpha = 0.10f)
    val shadow = if (darkTheme) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.24f)
    val gradientColors = remember(base, shadow) { listOf(base, shadow, base) }
    var leftInRoot by remember { mutableFloatStateOf(0f) }
    var rootWidth by remember { mutableFloatStateOf(0f) }

    Box(
        modifier
            .onGloballyPositioned { coordinates ->
                leftInRoot = coordinates.positionInRoot().x
                rootWidth = coordinates.findRootCoordinates().size.width.toFloat()
            }
            .drawBehind {
                val width = maxOf(rootWidth, size.width, 1f)
                val halfBand = width * 0.24f
                val center = shimmer.sweepProgress() * (width + 2 * halfBand) - halfBand - leftInRoot
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors,
                        startX = center - halfBand,
                        endX = center + halfBand,
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                )
            },
    )
}

@Composable
internal fun SevereOutlookSkeleton(shimmer: SkeletonShimmer, containerColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(3.73f),
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(shimmer, Modifier.width(90.dp).height(12.dp))
                SkeletonBlock(shimmer, Modifier.width(120.dp).height(21.dp))
            }
            SkeletonBlock(shimmer, Modifier.fillMaxWidth(0.46f).height(46.dp), 12.dp)
        }
    }
}

@Composable
internal fun AlertStatusSkeleton(shimmer: SkeletonShimmer, containerColor: Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(shimmer, Modifier.size(28.dp), 14.dp)
            Box(Modifier.weight(1f)) {
                SkeletonBlock(shimmer, Modifier.fillMaxWidth(0.65f).height(16.dp))
            }
            SkeletonBlock(shimmer, Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun StormAiButtonSkeleton(shimmer: SkeletonShimmer, containerColor: Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(shimmer, Modifier.size(24.dp))
            Box(Modifier.weight(1f)) {
                SkeletonBlock(shimmer, Modifier.width(104.dp).height(16.dp))
            }
            SkeletonBlock(shimmer, Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun StormSpecsSkeleton(shimmer: SkeletonShimmer, containerColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(2) { row ->
            val rowShimmer = shimmer.delayedBy(row * 50)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = containerColor,
                        shape = RoundedCornerShape(40.dp),
                    ) {
                        Row(
                            modifier = Modifier.height(59.dp).padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SkeletonBlock(rowShimmer, Modifier.size(24.dp), 12.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                SkeletonBlock(rowShimmer, Modifier.width(60.dp).height(10.dp))
                                SkeletonBlock(rowShimmer, Modifier.width(45.dp).height(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FiveDayOutlookSkeleton(shimmer: SkeletonShimmer, containerColor: Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
            repeat(5) { day ->
                val rowShimmer = shimmer.delayedBy(day * 50)
                if (day > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().height(75.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonBlock(rowShimmer, Modifier.weight(0.85f).height(14.dp))
                    SkeletonBlock(rowShimmer, Modifier.size(28.dp), 14.dp)
                    repeat(2) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            SkeletonBlock(rowShimmer, Modifier.width(32.dp).height(14.dp))
                            SkeletonBlock(rowShimmer, Modifier.width(24.dp).height(9.dp))
                        }
                    }
                    SkeletonBlock(rowShimmer, Modifier.weight(1.15f).height(26.dp), 13.dp)
                }
            }
        }
    }
}

@Composable
internal fun LoadingLocationLabel(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "Location loading dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Loading", color = color, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        repeat(3) { index ->
            val delay = index * 140
            val bounce = transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(keyframes {
                    durationMillis = 920
                    0f at delay
                    1f at (delay + 140)
                    0f at (delay + 280)
                }),
                label = "Location dot ${index + 1}",
            )
            Box(
                Modifier.size(4.dp)
                    .graphicsLayer { translationY = -5.dp.toPx() * bounce.value }
                    .background(color, CircleShape),
            )
        }
    }
}
