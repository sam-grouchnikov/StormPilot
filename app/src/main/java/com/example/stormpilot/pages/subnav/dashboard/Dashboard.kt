package com.example.stormpilot.pages.subnav.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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

@Composable
fun RadarPage(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    var isDarkMode by remember { mutableStateOf(true) }

    StormPilotTheme(darkTheme = isDarkMode) {
        Column(modifier = Modifier.padding(top = 15.dp)) {
            LocationWidget(viewModel)
            Spacer(modifier = Modifier.height(15.dp))
            AlertsWidget(viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            AtmosphereMetricsWidget()
            Spacer(modifier = Modifier.height(15.dp))
            ConnectivityWidget()
            Spacer(modifier = Modifier.height(15.dp))
            AssistanceWidget()
        }
    }
}