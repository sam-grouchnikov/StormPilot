package com.example.stormpilot.features.auth.ui


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.stormpilot.ui.theme.StormPilotTheme


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
        label = "logo_offset_animation"
    )

    LaunchedEffect(Unit) {
        delay(80)
        contentVisible.value = true
    }

    StormPilotTheme(
        dynamicColor = false,
        useSurfaceContainerNavigationBar = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 100.dp, top = 160.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Thunderstorm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(70.dp)
                            .graphicsLayer { translationY = logoOffset }
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "StormPilot",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 45.sp,
                        modifier = Modifier.staggeredEntrance(contentVisible.value, 0)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Your voice-first storm chasing hub",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.W500,
                    lineHeight = 38.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 63.dp)
                        .align(Alignment.Start)
                        .staggeredEntrance(contentVisible.value, 1)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth(0.81f)
                        .height(50.dp)
                        .staggeredEntrance(contentVisible.value, 2),
                    shape = RoundedCornerShape(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth(0.81f)
                        .height(50.dp)
                        .staggeredEntrance(contentVisible.value, 3),
                    shape = RoundedCornerShape(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Log In",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.staggeredEntrance(visible: Boolean, index: Int): Modifier {
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 120 * index,
            easing = FastOutSlowInEasing
        ),
        label = "welcome_alpha_$index"
    )
    val translation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 0f else 34f,
        animationSpec = tween(
            durationMillis = 520,
            delayMillis = 120 * index,
            easing = FastOutSlowInEasing
        ),
        label = "welcome_translation_$index"
    )

    return this.graphicsLayer {
        this.alpha = alpha
        translationY = translation
    }
}
