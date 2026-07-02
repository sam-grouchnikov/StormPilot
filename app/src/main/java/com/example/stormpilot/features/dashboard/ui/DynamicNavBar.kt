package com.example.stormpilot.features.dashboard.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertDetailBottomSheet
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertStatusPanel
import com.example.stormpilot.features.dashboard.ui.alerts.components.LocationAlertsMapCard
import com.example.stormpilot.features.dashboard.ui.alerts.components.StormSpecsPanel
import com.example.stormpilot.features.dashboard.ui.weather.FiveDayOutlook
import com.example.stormpilot.features.dashboard.ui.weather.HourlyForecastRow
import com.example.stormpilot.features.shared.data.alerts.bestMatchForEvent
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.features.shared.viewmodels.MapsViewModel
import com.example.stormpilot.features.shared.viewmodels.WeatherViewModel
import com.example.stormpilot.ui.theme.extendedColors
import org.maplibre.spatialk.geojson.Position

@Composable
fun DashboardOverviewScreen(
    modifier: Modifier = Modifier,
    widgetContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    mapsViewModel: MapsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val alertsState by alertsViewModel.uiState.collectAsStateWithLifecycle()
    val cityName by alertsViewModel.cityName.collectAsStateWithLifecycle()
    val locationGeoJson by alertsViewModel.locationGeoJson.collectAsStateWithLifecycle()
    val location by alertsViewModel.locationRepository.location.collectAsStateWithLifecycle()
    val mapsState by mapsViewModel.uiState.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val weatherCityName by weatherViewModel.cityName.collectAsStateWithLifecycle()
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
                .padding(top = 75.dp, start = 10.dp, end = 10.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = dashboardBottomContentPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
//            LocationAlertsMapCard(
//                cityName = cityName,
//                position = location?.let { Position(longitude = it.longitude, latitude = it.latitude) },
//                locationGeoJson = locationGeoJson,
//                alertsGeoJson = mapsState.alertsGeoJson,
//                onAlertPolygonClick = { point, eventType ->
//                    val localMatch = alertsState.allAlerts.bestMatchForEvent(eventType)
//                    if (localMatch != null) {
//                        mapsViewModel.showAlertDetail(localMatch)
//                    } else {
//                        mapsViewModel.onAlertPolygonTapped(point, eventType)
//                    }
//                },
//            )
//            ForecastSectionTitle("Alert Radar")
            AlertStatusPanel(
                alertsState = alertsState,
                onAlertClick = mapsViewModel::showAlertDetail,
                containerColor = widgetContainerColor,
            )
//            ForecastSectionTitle("Storm Environment")
            StormSpecsPanel(
                stormSpecs = weatherState.stormSpecs,
                containerColor = widgetContainerColor,
            )

            FiveDayOutlook(
                daily = weatherState.daily,
                containerColor = widgetContainerColor,
            )

            val hourly = weatherState.hourly
            HourlyForecastRow(
                hourly = hourly,
                containerColor = widgetContainerColor,
            )
//
//            WeatherOverviewSection(
//                cityName = weatherCityName,
//                uiState = weatherState,
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
private fun dashboardBottomContentPadding() =
    112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()


