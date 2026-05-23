package com.example.stormpilot

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.core.updateAppCompatNightMode
import com.example.stormpilot.features.auth.ui.SignIn
import com.example.stormpilot.features.auth.ui.SignUp
import com.example.stormpilot.features.auth.ui.WelcomePage
import com.example.stormpilot.features.auth.ui.NavSkeleton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        updateAppCompatNightMode(AppSettings.mode)
        super.onCreate(savedInstanceState)

        val semiTransparentScrim = android.graphics.Color.argb(0x90, 0x1b, 0x1b, 0x1b)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        window.statusBarColor = semiTransparentScrim

        setContent {
            StormPilotTheme {
                AppCenter()
            }
        }
    }
}

@Composable
fun AppCenter() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 5 },
                animationSpec = tween(durationMillis = 550)
            ) + fadeIn(animationSpec = tween(durationMillis = 500))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 6 },
                animationSpec = tween(durationMillis = 500)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 5 },
                animationSpec = tween(durationMillis = 500)
            ) + fadeIn(animationSpec = tween(durationMillis = 450))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 5 },
                animationSpec = tween(durationMillis = 450)
            ) + fadeOut(animationSpec = tween(durationMillis = 280))
        }
    ) {
        composable("welcome") {
            WelcomePage(
                onSignInClick = { navController.navigate("signin") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        composable("signin") {
            SignIn(
                onLogin = { navController.navigate("skeleton") }
            )
        }

        composable("signup") {
            SignUp(
                onLogin = { navController.navigate("skeleton") }
            )
        }

        composable("skeleton") {
            NavSkeleton()
        }
    }
}
