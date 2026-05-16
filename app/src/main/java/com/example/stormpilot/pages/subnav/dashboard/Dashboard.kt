package com.example.stormpilot.pages.subnav.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.subnav.dashboard.widgets.AlertsWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.AtmosphereMetricsWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.ConnectivityWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.LocationWidget
import com.example.stormpilot.viewmodel.AlertsViewModel
import com.example.stormpilot.genai.GenAIViewModel
import com.example.stormpilot.pages.subnav.dashboard.aichat.ChatPopup
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    genAIViewModel: GenAIViewModel = hiltViewModel()
) {
    var isDarkMode by remember { mutableStateOf(true) }
    val showWidgets = remember { mutableStateOf(false) }

    // Inside Dashboard Composable
    var showChat by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        delay(80)
        showWidgets.value = true
    }

    StormPilotTheme(darkTheme = isDarkMode) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showChat = true },
                    modifier = Modifier.padding(bottom = 0.dp, end = 5.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Ask Weather Pilot")
                }
            }
        ) {
            Column(modifier = Modifier.padding(top = 15.dp)) {
                AnimatedVisibility(
                    visible = showWidgets.value,
                    enter = fadeIn(animationSpec = tween(350, delayMillis = 0)) +
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(450, delayMillis = 0)
                            )
                ) { LocationWidget(alertsViewModel) }
                Spacer(modifier = Modifier.height(15.dp))
                AnimatedVisibility(
                    visible = showWidgets.value,
                    enter = fadeIn(animationSpec = tween(350, delayMillis = 60)) +
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(450, delayMillis = 60)
                            )
                ) { AlertsWidget(alertsViewModel) }
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedVisibility(
                    visible = showWidgets.value,
                    enter = fadeIn(animationSpec = tween(350, delayMillis = 120)) +
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(450, delayMillis = 120)
                            )
                ) { AtmosphereMetricsWidget() }
                Spacer(modifier = Modifier.height(15.dp))
                AnimatedVisibility(
                    visible = showWidgets.value,
                    enter = fadeIn(animationSpec = tween(350, delayMillis = 180)) +
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(450, delayMillis = 180)
                            )
                ) { ConnectivityWidget() }
            }

            if (showChat) {
                ChatPopup({showChat = false}, genAIViewModel)
            }
        }

    }
}
