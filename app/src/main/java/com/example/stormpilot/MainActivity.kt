package com.example.stormpilot

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.NavSkeleton
import com.example.stormpilot.pages.SignIn
import com.example.stormpilot.pages.SignUp
import com.example.stormpilot.pages.WelcomePage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
// Configure the behavior of the hidden bars (how they reappear)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

// Hide the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
//            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
//        )

        setContent {
            StormPilotTheme(darkTheme = true) {
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
