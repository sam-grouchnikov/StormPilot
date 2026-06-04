package com.example.stormpilot.features.auth.ui.components

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.stormpilot.ui.theme.StormPilotTheme
import kotlinx.coroutines.delay

/**
 * Shared auth shell that keeps sign-in and sign-up visually aligned while the
 * leaf screen files only define copy and navigation actions.
 */
@Composable
fun AuthExperienceScreen(
    modeTitle: String,
    heroText: String,
    switchPrompt: String,
    switchActionLabel: String,
    primaryButtonLabel: String,
    onPrimaryAction: () -> Unit,
    onSwitchMode: () -> Unit,
) {
    val contentVisible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(90)
        contentVisible.value = true
    }

    StormPilotTheme(
        dynamicColor = false,
        useSurfaceContainerNavigationBar = true,
    ) {
        val scheme = MaterialTheme.colorScheme
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = scheme.surface,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scheme.surfaceBright,
                                scheme.surfaceContainer,
                                scheme.surface,
                            ),
                        ),
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                AnimatedStormBackdrop()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        modifier = Modifier.signInEntrance(contentVisible.value, 3),
                        shape = RoundedCornerShape(36.dp),
                        color = scheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        tonalElevation = 6.dp,
                        shadowElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                        ) {
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = scheme.primary,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AuthModeSwitchRow(
                                prompt = switchPrompt,
                                actionLabel = switchActionLabel,
                                onClick = onSwitchMode,
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            ExpressiveInputField(
                                value = email,
                                onValueChange = { email = it },
                                icon = Icons.Outlined.Email,
                                label = "Email",
                                placeholder = "stormchaser@domain.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            ExpressiveInputField(
                                value = password,
                                onValueChange = { password = it },
                                icon = Icons.Outlined.Lock,
                                label = "Password",
                                placeholder = "••••••••",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation(),
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
                                    contentColor = scheme.onPrimary,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = primaryButtonLabel,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
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
