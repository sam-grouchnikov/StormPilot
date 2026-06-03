package com.example.stormpilot.features.auth.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.ui.theme.StormPilotTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WelcomePage(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    val contentVisible = remember { mutableStateOf(false) }
    val bobbingTransition = rememberInfiniteTransition(label = "welcome_logo_bobbing")
    val logoOffset by bobbingTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome_logo_offset"
    )

    LaunchedEffect(Unit) {
        delay(80)
        contentVisible.value = true
    }

    StormPilotTheme(
        dynamicColor = false,
        useSurfaceContainerNavigationBar = true
    ) {
        val scheme = MaterialTheme.colorScheme

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = scheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scheme.surfaceBright,
                                scheme.surfaceContainer,
                                scheme.surface
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                WelcomeStormBackdrop()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Modern storm chasing, with a touch of AI.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 50.sp
                        ),
                        color = scheme.onSurface,
                        modifier = Modifier.welcomeEntrance(contentVisible.value, 1)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Voice-supported commands, smarter routing, and chaser-oriented data.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.welcomeEntrance(contentVisible.value, 2)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier.welcomeEntrance(contentVisible.value, 3),
                        shape = RoundedCornerShape(36.dp),
                        color = scheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        tonalElevation = 6.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = scheme.primary
                            )


                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onSignUpClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.surfaceBright,
                                    contentColor = scheme.onSurface
                                )
                            ) {
                                Text(
                                    text = "Sign up",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onSignInClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = scheme.primary,
                                    contentColor = scheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Log in",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStormBackdrop() {
    val scheme = MaterialTheme.colorScheme
    val drift = rememberInfiniteTransition(label = "welcome_backdrop")
    val xShift by drift.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome_backdrop_x"
    )
    val yShift by drift.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome_backdrop_y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .welcomeOffsetFromAnimation(x = xShift, y = yShift)
                .size(240.dp)
                .blur(18.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = 0.24f),
                            scheme.tertiary.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(140f, 140f)
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .welcomeOffsetFromAnimation(x = -xShift * 0.6f, y = -yShift * 0.45f)
                .size(220.dp)
                .blur(24.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            scheme.secondary.copy(alpha = 0.18f),
                            scheme.primaryContainer.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(120f, 120f)
                    )
                )
        )
    }
}

@Composable
private fun Modifier.welcomeEntrance(visible: Boolean, index: Int): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing
        ),
        label = "welcome_alpha_$index"
    )
    val y by animateFloatAsState(
        targetValue = if (visible) 0f else 28f,
        animationSpec = tween(
            durationMillis = 560,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing
        ),
        label = "welcome_y_$index"
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = y
    }
}

@Composable
private fun Modifier.welcomeOffsetFromAnimation(x: Float, y: Float): Modifier {
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(x.roundToInt(), y.roundToInt()),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "welcome_storm_offset"
    )
    return this.then(
        Modifier.graphicsLayer {
            translationX = animatedOffset.x.toFloat()
            translationY = animatedOffset.y.toFloat()
        }
    )
}
