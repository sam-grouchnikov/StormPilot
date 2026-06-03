package com.example.stormpilot.features.dashboard.ui.alerts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.features.alerts.presentation.AlertsViewModel
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertStatusPanel
import com.example.stormpilot.features.dashboard.ui.alerts.components.LocationAlertsMapCard
import com.example.stormpilot.features.dashboard.ui.alerts.components.StormSpecsPanel
import com.example.stormpilot.features.map.presentation.MapsViewModel
import com.example.stormpilot.features.weather.presentation.WeatherViewModel
import org.maplibre.spatialk.geojson.Position

@Composable
fun AlertSlide(
    title: String,
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) alertsViewModel.locationRepository.startTracking()
    }

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            alertsViewModel.locationRepository.startTracking()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LocationAlertsMapCard(
            cityName = cityName,
            position = location?.let { Position(longitude = it.longitude, latitude = it.latitude) },
            locationGeoJson = locationGeoJson,
            alertsGeoJson = mapsState.alertsGeoJson,
        )

        AlertStatusPanel(alertsState = alertsState)
        StormSpecsPanel(stormSpecs = weatherState.stormSpecs)
    }
}
