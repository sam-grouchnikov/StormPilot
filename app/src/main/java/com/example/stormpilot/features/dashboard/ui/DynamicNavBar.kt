package com.example.stormpilot.features.dashboard.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.R
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertDetailBottomSheet
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertStatusPanel
import com.example.stormpilot.features.dashboard.ui.alerts.components.StormSpecsPanel
import com.example.stormpilot.features.dashboard.ui.weather.FiveDayOutlook
import com.example.stormpilot.features.dashboard.ui.weather.HourlyForecastRow
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.features.shared.viewmodels.MapsViewModel
import com.example.stormpilot.features.shared.viewmodels.WeatherViewModel

@Composable
fun DashboardOverviewScreen(
    modifier: Modifier = Modifier,
    widgetContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    mapsViewModel: MapsViewModel = hiltViewModel(),
    onOpenStormAiChat: () -> Unit,
) {
    val context = LocalContext.current
    val alertsState by alertsViewModel.uiState.collectAsStateWithLifecycle()
    val mapsState by mapsViewModel.uiState.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val useDashboardPlaceholderData = AppSettings.useDashboardPlaceholderData
    val dashboardPlaceholderAssetName = AppSettings.dashboardPlaceholderWeatherAssetName

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted && !useDashboardPlaceholderData) {
            alertsViewModel.locationRepository.startTracking()
            weatherViewModel.locationRepository.startTracking()
        }
    }

    LaunchedEffect(useDashboardPlaceholderData, dashboardPlaceholderAssetName) {
        alertsViewModel.setDashboardPlaceholderMode(
            enabled = useDashboardPlaceholderData,
            assetName = dashboardPlaceholderAssetName,
        )
        mapsViewModel.setDashboardPlaceholderMode(useDashboardPlaceholderData)
        weatherViewModel.setDashboardPlaceholderSource(
            enabled = useDashboardPlaceholderData,
            assetName = dashboardPlaceholderAssetName,
        )
    }

    LaunchedEffect(useDashboardPlaceholderData) {
        if (useDashboardPlaceholderData) return@LaunchedEffect

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            alertsViewModel.locationRepository.startTracking()
            weatherViewModel.locationRepository.startTracking()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 82.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = dashboardBottomContentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SevereOutlookBanner(weatherState.daily.firstOrNull()?.spcOutlook)

            AlertStatusPanel(
                alertsState = alertsState,
                onAlertClick = mapsViewModel::showAlertDetail,
                containerColor = widgetContainerColor,
            )
            AskStormAiButton(onClick = onOpenStormAiChat)
            StormSpecsPanel(
                stormSpecs = weatherState.stormSpecs,
                current = weatherState.current,
                containerColor = widgetContainerColor,
            )
//
            FiveDayOutlook(
                daily = weatherState.daily,
                containerColor = widgetContainerColor,
            )
//
//            val hourly = weatherState.hourly
//            HourlyForecastRow(
//                hourly = hourly,
//                containerColor = widgetContainerColor,
//            )
        }

        AlertDetailBottomSheet(
            isVisible = mapsState.isAlertDetailVisible,
            alert = mapsState.selectedAlert,
            isLoading = mapsState.isAlertDetailLoading,
            errorMessage = mapsState.alertDetailError,
            onDismiss = mapsViewModel::dismissAlertDetail,
        )
    }
}

@Composable
private fun SevereOutlookBanner(outlook: String?) {
    val risk = outlook?.trim().orEmpty()
    val (label, artwork) = when {
        risk.equals("HIGH", true) || risk.startsWith("High ", true) ->
            "High" to R.drawable.high_outlook_banner_cartoon
        risk.equals("MDT", true) || risk.startsWith("Moderate", true) ->
            "Moderate" to R.drawable.moderate_risk_banner_cartoon
        risk.equals("ENH", true) || risk.startsWith("Enhanced", true) ->
            "Enhanced" to R.drawable.enhanced_outlook_banner_cartoon
        risk.equals("SLGT", true) || risk.startsWith("Slight", true) ->
            "Slight" to R.drawable.slight_outlook_banner_cartoon
        risk.equals("MRGL", true) || risk.startsWith("Marginal", true) ->
            "Marginal" to R.drawable.marginal_outlook_banner_cartoon
        risk.startsWith("TSTM", true) || risk.startsWith("General Thunderstorms", true) ->
            "Thunderstorms" to R.drawable.no_risk_outlook_banner_cartoon
        else -> risk.ifEmpty { "Unavailable" } to R.drawable.no_risk_outlook_banner_cartoon
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3.73f)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Image(
            painter = painterResource(artwork),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.padding(start = 28.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Severe Outlook", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            Text(label, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun dashboardBottomContentPadding() =
    112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
