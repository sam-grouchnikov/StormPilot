package com.example.stormpilot.features.auth.ui

import androidx.compose.runtime.Composable
import com.example.stormpilot.features.auth.ui.components.AuthExperienceScreen

@Composable
fun SignIn(onLogin: () -> Unit, onSwitchToSignUp: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign in",
        heroText = "Modern storm chasing, with a touch of AI.",
        switchPrompt = "No account yet?",
        switchActionLabel = "Sign up",
        primaryButtonLabel = "Launch into StormPilot",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignUp,
    )
}
