package com.example.stormpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.NavSkeleton
import com.example.stormpilot.pages.WelcomePage
import com.example.stormpilot.pages.SignIn
import com.example.stormpilot.pages.SignUp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            StormPilotTheme(darkTheme = true) {
                StormPilotApp()
            }
        }
    }
}

@Composable
fun StormPilotApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
            NavSkeleton(

            )
        }


    }
}