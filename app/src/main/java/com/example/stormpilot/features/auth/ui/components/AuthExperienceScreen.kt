package com.example.stormpilot.features.auth.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.stormpilot.R
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

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
    StormPilotTheme(
        darkTheme = true,
        dynamicColor = false,
        opaqueNavigationBar = false,
        useSurfaceContainerNavigationBar = true,
        syncAppDarkMode = false,
    ) {
        val colors = MaterialTheme.extendedColors
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.welcomeNavyStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.welcomeNavyStart,
                                colors.welcomeNavyEnd,
                            ),
                        ),
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "StormPilot logo",
                        modifier = Modifier.height(92.dp),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = modeTitle,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 42.sp,
                        ),
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = heroText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                        ),
                        color = Color.White.copy(alpha = 0.74f),
                    )

                    Spacer(modifier = Modifier.height(34.dp))

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
                        placeholder = "Password",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onPrimaryAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colors.welcomeNavyStart,
                        ),
                    ) {
                        Text(
                            text = primaryButtonLabel,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AuthModeSwitchRow(
                        prompt = switchPrompt,
                        actionLabel = switchActionLabel,
                        onClick = onSwitchMode,
                    )
                }
            }
        }
    }
}
