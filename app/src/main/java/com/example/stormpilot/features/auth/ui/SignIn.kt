package com.example.stormpilot.features.auth.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.ui.theme.StormPilotTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SignIn(onLogin: () -> Unit, onSwitchToSignUp: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign in",
        heroText = "Modern storm chasing, with a touch of AI.",
        switchPrompt = "No account yet?",
        switchActionLabel = "Sign up",
        primaryButtonLabel = "Launch into StormPilot",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignUp
    )
}

@Composable
fun SignUp(onLogin: () -> Unit, onSwitchToSignIn: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign up",
        heroText = "Modern storm chasing, with a touch of AI.",
        switchPrompt = "Already have an account?",
        switchActionLabel = "Sign in",
        primaryButtonLabel = "Create your account",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignIn
    )
}

@Composable
private fun AuthExperienceScreen(
    modeTitle: String,
    heroText: String,
    switchPrompt: String,
    switchActionLabel: String,
    primaryButtonLabel: String,
    onPrimaryAction: () -> Unit,
    onSwitchMode: () -> Unit
) {
    val contentVisible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(90)
        contentVisible.value = true
    }

    StormPilotTheme(
        dynamicColor = false,
        useSurfaceContainerNavigationBar = true
    ) {
        val scheme = MaterialTheme.colorScheme
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

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
                AnimatedStormBackdrop()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = heroText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 50.sp
                        ),
                        color = scheme.onSurface,
                        modifier = Modifier.signInEntrance(contentVisible.value, 1)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier.signInEntrance(contentVisible.value, 3),
                        shape = RoundedCornerShape(36.dp),
                        color = scheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        tonalElevation = 6.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = scheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AuthModeSwitchRow(
                                prompt = switchPrompt,
                                actionLabel = switchActionLabel,
                                onClick = onSwitchMode
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            ExpressiveInputField(
                                value = email,
                                onValueChange = { email = it },
                                icon = Icons.Outlined.Email,
                                label = "Email",
                                placeholder = "stormchaser@domain.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            ExpressiveInputField(
                                value = password,
                                onValueChange = { password = it },
                                icon = Icons.Outlined.Lock,
                                label = "Password",
                                placeholder = "••••••••",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = onPrimaryAction,
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
                                        text = primaryButtonLabel,
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
private fun AuthModeSwitchRow(
    prompt: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            color = scheme.onSurfaceVariant
        )

        TextButton(onClick = onClick) {
            Text(
                text = actionLabel,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun AnimatedStormBackdrop() {
    val scheme = MaterialTheme.colorScheme
    val drift = rememberInfiniteTransition(label = "storm_backdrop")
    val xShift by drift.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "storm_backdrop_x"
    )
    val yShift by drift.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "storm_backdrop_y"
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
                            Color.Transparent
                        ),
                        center = Offset(140f, 140f)
                    )
                )
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
                            Color.Transparent
                        ),
                        center = Offset(120f, 120f)
                    )
                )
        )
    }
}


@Composable
private fun Modifier.signInEntrance(visible: Boolean, index: Int): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing
        ),
        label = "signin_alpha_$index"
    )
    val y by animateFloatAsState(
        targetValue = if (visible) 0f else 28f,
        animationSpec = tween(
            durationMillis = 560,
            delayMillis = index * 90,
            easing = FastOutSlowInEasing
        ),
        label = "signin_y_$index"
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = y
    }
}

@Composable
private fun ExpressiveInputField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val glowColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary.copy(alpha = 0.34f) else Color.Transparent,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_glow_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary.copy(alpha = 0.55f) else scheme.outlineVariant.copy(alpha = 0.18f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_border_color"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "input_shadow_elevation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Surface(
            modifier = Modifier
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = glowColor,
                    ambientColor = glowColor
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceBright.copy(alpha = 0.86f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .heightIn(min = 56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(scheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    textStyle = TextStyle(
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (placeholder != null && value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = scheme.outline
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LinedIconInputField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    startPadding: Int = 7,
) {
    ExpressiveInputField(
        value = value,
        onValueChange = onValueChange,
        icon = icon,
        label = "",
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}

@Composable
private fun Modifier.offsetFromAnimation(x: Float, y: Float): Modifier {
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(x.roundToInt(), y.roundToInt()),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "storm_offset"
    )
    return this.then(
        Modifier.graphicsLayer {
            translationX = animatedOffset.x.toFloat()
            translationY = animatedOffset.y.toFloat()
        }
    )
}
