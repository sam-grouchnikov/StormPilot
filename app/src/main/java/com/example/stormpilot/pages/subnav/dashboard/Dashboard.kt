package com.example.stormpilot.pages.subnav.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun RadarPage(
    // It's best practice to pass the ViewModel as a parameter
    // This makes the Composable easier to preview and test
    viewModel: AlertsViewModel = hiltViewModel()
) {
    var isDarkMode by remember { mutableStateOf(true) }


    StormPilotTheme(darkTheme = isDarkMode) {
        Column {
            // Pass the ViewModel or specific state to your widgets
            LocationWidget(viewModel)
            Spacer(modifier = Modifier.height(15.dp))
            AlertsWidget()
            Spacer(modifier = Modifier.height(10.dp))
            AtmosphereMetricsWidget()
            Spacer(modifier = Modifier.height(15.dp))
            ConnectivityWidget()
            Spacer(modifier = Modifier.height(15.dp))
            AssistanceWidget()
        }
    }
}