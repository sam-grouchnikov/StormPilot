package com.example.stormpilot.features.auth.ui

import androidx.compose.runtime.Composable
import com.example.stormpilot.features.auth.ui.components.AuthExperienceScreen

@Composable
fun SignIn(onLogin: () -> Unit, onSwitchToSignUp: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign in",
        heroText = "Get back to your alerts, routes, and chase plan.",
        switchPrompt = "No account yet?",
        switchActionLabel = "Sign up",
        primaryButtonLabel = "Sign in",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignUp,
    )
}
