package com.example.stormpilot.pages.subnav.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.stormpilot.pages.subnav.dashboard.widgets.AssistanceWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.AtmosphereMetricsWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.ConnectivityWidget
import com.example.stormpilot.pages.subnav.dashboard.widgets.LocationWidget
import com.example.stormpilot.viewmodel.AlertsViewModel
import kotlinx.coroutines.delay

@Composable
fun RadarPage(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    var isDarkMode by remember { mutableStateOf(true) }
    val showWidgets = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        showWidgets.value = true
    }

    StormPilotTheme(darkTheme = isDarkMode) {
        Column(modifier = Modifier.padding(top = 15.dp)) {
            AnimatedVisibility(
                visible = showWidgets.value,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 0)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(450, delayMillis = 0)
                    )
            ) { LocationWidget(viewModel) }
            Spacer(modifier = Modifier.height(15.dp))
            AnimatedVisibility(
                visible = showWidgets.value,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 60)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(450, delayMillis = 60)
                    )
            ) { AlertsWidget(viewModel) }
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
            Spacer(modifier = Modifier.height(15.dp))
            AnimatedVisibility(
                visible = showWidgets.value,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 240)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(450, delayMillis = 240)
                    )
            ) { AssistanceWidget() }
        }
    }
}
