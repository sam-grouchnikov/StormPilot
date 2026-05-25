package com.example.stormpilot.features.dashboard.ui.alerts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Flood
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.data.StormSpec
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.data.AlertsUiState
import com.example.stormpilot.data.AlertsViewModel
import com.example.stormpilot.data.WeatherViewModel
import com.example.stormpilot.data.MapsViewModel
import kotlin.time.Duration.Companion.milliseconds
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
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
        if (ContextCompat.checkSelfPermission(
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
            .verticalScroll(rememberScrollState()),

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

@Composable
private fun LocationAlertsMapCard(
    cityName: String,
    position: Position?,
    locationGeoJson: GeoJsonData,
    alertsGeoJson: GeoJsonData?,
) {
    var showAlerts by remember { mutableStateOf(true) }

    val extendedColors = ExtendedColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = extendedColors.blueBackground,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp),
                )
                Text(
                    text = cityName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.W600,
                    fontSize = 19.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { showAlerts = !showAlerts },
                    modifier = Modifier.size(30.dp).padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = if (showAlerts) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LocationAlertsMap(
                position = position,
                locationGeoJson = locationGeoJson,
                alertsGeoJson = alertsGeoJson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                showAlerts
            )
        }
    }
}

@Composable
private fun LocationAlertsMap(
    position: Position?,
    locationGeoJson: GeoJsonData,
    alertsGeoJson: GeoJsonData?,
    modifier: Modifier = Modifier,
    showAlerts: Boolean
) {
    var radarRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val mapTarget = position ?: Position(latitude = 39.8283, longitude = -98.5795)

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = mapTarget,
            zoom = 9.0,
        ),
    )

    LaunchedEffect(position) {
        position?.let { target ->
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = target,
                    zoom = 8.0,
                    tilt = 0.0,
                    bearing = 0.0,
                ),
                duration = 650.milliseconds,
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(300_000L)
            radarRefreshKey = System.currentTimeMillis()
        }
    }

    val mapStyle = if (AppSettings.isDarkMode) {
        "https://api.protomaps.com/styles/v5/dark/en.json?key=64a5f0a9c35b4ca1"
    } else {
        "https://api.protomaps.com/styles/v5/white/en.json?key=64a5f0a9c35b4ca1"
    }

    MaplibreMap(
        baseStyle = BaseStyle.Uri(mapStyle),
        cameraState = cameraState,
        modifier = modifier,
        options = MapOptions(
            gestureOptions = GestureOptions.AllDisabled,
            ornamentOptions = OrnamentOptions(
                padding = PaddingValues(0.dp),
                isLogoEnabled = false,
                isAttributionEnabled = false,
                isCompassEnabled = false,
                isScaleBarEnabled = false,
            ),
        ),
    ) {
        val fallbackJson = """{"type":"FeatureCollection","features":[]}"""
        val safeGeoJson = alertsGeoJson ?: GeoJsonData.JsonString(fallbackJson)

        val alertsSource = rememberGeoJsonSource(
            data = safeGeoJson
        )

        if (showAlerts) {
            FillLayer(
                id = "location-alerts-fill",
                source = alertsSource,
                color = switch(
                    condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(0x22FF0000))),
                    condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(0x22FFD700))),
                    condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(0x2200BB00))),
                    fallback = const(Color.Transparent),
                ),
            )

            LineLayer(
                id = "location-alerts-outline",
                source = alertsSource,
                color = switch(
                    condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(0xC8FF3030))),
                    condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(0xFFFFB020))),
                    condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(0xC800E676))),
                    fallback = const(Color.Transparent),
                ),
                width = const(2.dp),
            )
        }



        val locationSource = rememberGeoJsonSource(data = locationGeoJson)
        CircleLayer(
            id = "location-current-point",
            source = locationSource,
            color = const(MaterialTheme.colorScheme.onPrimary),
            radius = const(5.dp),
            strokeColor = const(MaterialTheme.colorScheme.primary),
            strokeWidth = const(3.dp),
        )
    }
}

@Composable
private fun StormSpecsPanel(stormSpecs: List<StormSpec>) {
    val colors = ExtendedColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color =  colors.stormContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Storm Environment",
                color = colors.stormText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (stormSpecs.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    LinearWavyProgressIndicator(
                        waveSpeed = 1.dp,
                        wavelength = 50.dp,
                        gapSize = 5.dp,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 5.dp),
                        color = colors.stormText,
                        trackColor = colors.stormContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                } else {
                    stormSpecs.forEach { spec ->
                        StormSpecCard(spec = spec)
                    }

                }
            }
        }
    }
}

private fun specToRisk(spec: StormSpec): String {
    val numericString = spec.value.replace(Regex("[^0-9.-]"), "")
    val value = numericString.toDoubleOrNull() ?: return "Unknown"

    return when (spec.label) {
        "CAPE" -> when {
            value < 1000 -> "Low"
            value in 1000.0..2500.0 -> "Moderate"
            value in 2501.0..4000.0 -> "High"
            else -> "Extreme"
        }

        "CIN" -> when {
            value > 100 -> "Low"
            value in 25.0..100.0 -> "Moderate"
            value in 1.0..24.0 -> "High"
            else -> "Extreme"
        }

        "SRH" -> when {
            value < 100 -> "Low"
            value in 100.0..250.0 -> "Moderate"
            value in 251.0..400.0 -> "High"
            else -> "Extreme"
        }

        "Lifted" -> when {
            value >= 0 -> "Low"
            value in -4.0..-1.0 -> "Moderate"
            value in -7.0..-5.0 -> "High"
            else -> "Extreme"
        }

        "LL Shear" -> when {
            value < 15 -> "Low"
            value in 15.0..25.0 -> "Moderate"
            value in 26.0..40.0 -> "High"
            else -> "Extreme"
        }

        "Dew Pt" -> when {
            value < 55 -> "Low"
            value in 55.0..64.0 -> "Moderate"
            value in 65.0..72.0 -> "High"
            else -> "Extreme"
        }

        "RH" -> when {
            value < 50 -> "Low"
            value in 50.0..70.0 -> "Moderate"
            value in 71.0..85.0 -> "High"
            else -> "Extreme"
        }

        "Gust" -> when {
            value < 40 -> "Low"
            value in 40.0..57.0 -> "Moderate"
            value in 58.0..74.0 -> "High"
            else -> "Extreme"
        }

        else -> "Unknown"
    }
}

@Composable
private fun StormSpecCard(spec: StormSpec) {
    val colors = ExtendedColors()
    val risk = specToRisk(spec)
    val extendedColors = ExtendedColors()
    val color = when (risk) {
        "Low" -> extendedColors.lowRisk
        "Moderate" -> extendedColors.moderateRisk
        "High" -> extendedColors.highRisk
        "Extreme" -> extendedColors.extremeRisk
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.width(118.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.stormContainerNested,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = spec.label,
                color = colors.stormTextNested,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
            )
            Text(
                text = spec.value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
            )
            Text(
                text = "$risk risk",
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
private fun AlertStatusPanel(alertsState: AlertsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AlertStatusRow(
                icon = Icons.Outlined.Tornado,
                label = "Tornado",
                state = alertState(
                    warningActive = alertsState.tornadoWarning != null,
                    watchActive = alertsState.tornadoWatch != null,
                    warningText = "Tornado Warning",
                    watchText = "Tornado Watch",
                    clearText = "No Tornado Alerts",
                ),
            )
            AlertStatusRow(
                icon = Icons.Outlined.Bolt,
                label = "Severe Thunderstorm",
                state = alertState(
                    warningActive = alertsState.severeThunderstormWarning != null,
                    watchActive = alertsState.severeThunderstormWatch != null,
                    warningText = "Severe T-Storm Warning",
                    watchText = "Severe T-Storm Watch",
                    clearText = "No Storm Alerts",
                ),
            )
            AlertStatusRow(
                icon = Icons.Outlined.Flood,
                label = "Flood",
                state = alertState(
                    warningActive = alertsState.flashFloodWarning != null,
                    watchActive = alertsState.flashFloodWatch != null,
                    warningText = "Flash Flood Warning",
                    watchText = "Flash Flood Watch",
                    clearText = "No Flood Alerts",
                ),
            )
        }
    }
}

@Composable
private fun AlertStatusRow(
    icon: ImageVector,
    label: String,
    state: LocationAlertState,
) {
    val colors = ExtendedColors()
    val containerTarget = when (state.level) {
        AlertLevel.Clear -> MaterialTheme.colorScheme.surfaceContainer
        AlertLevel.Watch -> colors.alertWatchContainer
        AlertLevel.Warning -> colors.alertWarningContainer
    }
    val contentTarget = when (state.level) {
        AlertLevel.Clear -> MaterialTheme.colorScheme.onSurfaceVariant
        AlertLevel.Watch -> colors.alertWatchContent
        AlertLevel.Warning -> colors.alertWarningContent
    }
    val titleTarget = when (state.level) {
        AlertLevel.Clear -> colors.alertClearContent
        AlertLevel.Watch -> colors.alertWatchContent
        AlertLevel.Warning -> colors.alertWarningContent
    }
    val iconTarget = when (state.level) {
        AlertLevel.Clear -> colors.alertClearContentAlternate
        AlertLevel.Watch -> colors.alertWatchContent
        AlertLevel.Warning -> colors.alertWarningContent
    }
    val containerColor by animateColorAsState(
        targetValue = containerTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_container_color",
    )
    val contentColor by animateColorAsState(
        targetValue = contentTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_content_color",
    )
    val titleColor by animateColorAsState(
        targetValue = titleTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_content_color",
    )
    val iconColor by animateColorAsState(
        targetValue = iconTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_icon_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                AnimatedContent(
                    targetState = state.text,
                    transitionSpec = {
                        fadeIn(tween(220, delayMillis = 70)) togetherWith fadeOut(tween(140))
                    },
                    label = "alert_status_text",
                ) { text ->
                    Text(
                        text = text,
                        color = contentColor,
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}

private enum class AlertLevel {
    Clear,
    Watch,
    Warning,
}

private data class LocationAlertState(
    val level: AlertLevel,
    val text: String,
)

private fun alertState(
    warningActive: Boolean,
    watchActive: Boolean,
    warningText: String,
    watchText: String,
    clearText: String,
): LocationAlertState = when {
    warningActive -> LocationAlertState(AlertLevel.Warning, warningText)
    watchActive -> LocationAlertState(AlertLevel.Watch, watchText)
    else -> LocationAlertState(AlertLevel.Clear, clearText)
}
