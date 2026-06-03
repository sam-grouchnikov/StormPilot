package com.example.stormpilot.features.auth.ui

import androidx.compose.runtime.Composable
import com.example.stormpilot.features.auth.ui.components.AuthExperienceScreen

@Composable
fun SignUp(onLogin: () -> Unit, onSwitchToSignIn: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign up",
        heroText = "Modern storm chasing, with a touch of AI.",
        switchPrompt = "Already have an account?",
        switchActionLabel = "Sign in",
        primaryButtonLabel = "Create your account",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignIn,
    )
}
