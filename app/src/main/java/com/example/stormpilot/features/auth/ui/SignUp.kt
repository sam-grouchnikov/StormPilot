package com.example.stormpilot.features.auth.ui

import androidx.compose.runtime.Composable
import com.example.stormpilot.features.auth.ui.components.AuthExperienceScreen

@Composable
fun SignUp(onLogin: () -> Unit, onSwitchToSignIn: () -> Unit) {
    AuthExperienceScreen(
        modeTitle = "Sign up",
        heroText = "Start with a more intelligent way to track storms on the move.",
        switchPrompt = "Already have an account?",
        switchActionLabel = "Sign in",
        primaryButtonLabel = "Create account",
        onPrimaryAction = onLogin,
        onSwitchMode = onSwitchToSignIn,
    )
}
