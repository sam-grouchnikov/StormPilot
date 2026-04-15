package com.example.stormpilot.pages.subnav.maps

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.subnav.maps.routing.formatDistance
import com.example.stormpilot.pages.subnav.maps.routing.formatDuration
import com.example.stormpilot.pages.subnav.maps.viewmodel.MapsUiState
import com.example.stormpilot.pages.subnav.maps.viewmodel.MapsViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsPage(
    viewModel: MapsViewModel = hiltViewModel(),
    onDestinationSelectedStateChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var navMode by remember { mutableStateOf(false) }
    var userIsInteracting by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = 39.8283, longitude = -98.5795),
            zoom = 3.0,
        ),
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
                    .setMinUpdateIntervalMillis(1_000L)
                    .build()

            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location ->
                            viewModel.onUserLocationUpdated(Position(location.longitude, location.latitude))
                        }
                    }
                }

            fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)

            onDispose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

    var hasInitialLocation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.origin) {
        uiState.origin?.let { location ->
            if (!hasInitialLocation) {
                hasInitialLocation = true
                cameraState.animateTo(
                    finalPosition = cameraState.position.copy(target = location, zoom = 16.0),
                    duration = 1.seconds,
                )
            }
        }
    }

    LaunchedEffect(uiState.routeGeoJson) {
        val origin = uiState.origin
        val destination = uiState.destination
        if (!navMode && origin != null && destination != null && uiState.routeGeoJson != null) {
            val center = Position(
                longitude = (origin.longitude + destination.longitude) / 2.0,
                latitude = (origin.latitude + destination.latitude) / 2.0,
            )
            val latDelta = abs(origin.latitude - destination.latitude)
            val lonDelta = abs(origin.longitude - destination.longitude)
            val paddingFactor = 5.5
            val span = (max(latDelta, lonDelta) * paddingFactor).coerceAtLeast(0.0005)
            val zoom = (9.5 - log(span, 2.0)).coerceIn(1.0, 15.5)
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(target = center, zoom = zoom),
                duration = 1.seconds,
            )
        }
    }

    LaunchedEffect(navMode, uiState.origin, uiState.currentStepIndex, uiState.steps) {
        if (!navMode) return@LaunchedEffect
        val origin = uiState.origin ?: return@LaunchedEffect
        if (userIsInteracting) return@LaunchedEffect
        val currentStep = uiState.steps.getOrNull(uiState.currentStepIndex)
        val bearing = currentStep?.maneuver?.bearingAfter?.toDouble() ?: cameraState.position.bearing
        cameraState.animateTo(
            finalPosition = cameraState.position.copy(
                target = origin,
                zoom = 17.5,
                tilt = 50.0,
                bearing = bearing,
            ),
            duration = 1.seconds,
        )
    }

    suspend fun resetCam(cameraState: CameraState) {
        delay(100)
        cameraState.animateTo(
            finalPosition = cameraState.position.copy(
                zoom = 16.0,
                tilt = 0.0,
                bearing = 0.0,
            ),
            duration = 1.seconds,
        )
    }

    LaunchedEffect(uiState.destination) {
        onDestinationSelectedStateChanged(uiState.destination != null)
    }

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var showTripSummary by remember {mutableStateOf(false)}


    StormPilotTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                baseStyle = BaseStyle.Uri("https://api.protomaps.com/styles/v5/dark/en.json?key=64a5f0a9c35b4ca1"),
                cameraState = cameraState,
                onMapLongClick = { point, _ ->
                    showTripSummary = true
                    viewModel.onDestinationSelected(point)
                    ClickResult.Consume
                },
                modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        padding = PaddingValues(0.dp),
                        isLogoEnabled = false,
                        logoAlignment = Alignment.BottomStart,
                        isAttributionEnabled = false,
                        attributionAlignment = Alignment.BottomEnd,
                        isCompassEnabled = false,
                        compassAlignment = Alignment.BottomEnd,
                        isScaleBarEnabled = false,
                        scaleBarAlignment = Alignment.TopStart,
                    ),
                ),
            ) {
                val userLocationFeatureCollection = remember(uiState.origin) {
                    val json = if (uiState.origin != null) {
                        """
                        {
                          "type": "FeatureCollection",
                          "features": [{
                            "type": "Feature",
                            "geometry": {
                              "type": "Point",
                              "coordinates": [${uiState.origin!!.longitude}, ${uiState.origin!!.latitude}]
                            },
                            "properties": {}
                          }]
                        }
                        """.trimIndent()
                    } else {
                        """{"type": "FeatureCollection", "features": []}"""
                    }
                    GeoJsonData.JsonString(json)
                }

                val userLocationSource = rememberGeoJsonSource(data = userLocationFeatureCollection)
                CircleLayer(
                    id = "user-location",
                    source = userLocationSource,
                    color = const(MaterialTheme.colorScheme.onPrimary),
                    radius = const(6.dp),
                    strokeColor = const(MaterialTheme.colorScheme.primary),
                    strokeWidth = const(3.dp),
                )

                val destinationFeatureCollection = remember(uiState.destination) {
                    val json = if (uiState.destination != null) {
                        """
                        {
                          "type": "FeatureCollection",
                          "features": [{
                            "type": "Feature",
                            "geometry": {
                              "type": "Point",
                              "coordinates": [${uiState.destination!!.longitude}, ${uiState.destination!!.latitude}]
                            },
                            "properties": {}
                          }]
                        }
                        """.trimIndent()
                    } else {
                        """{"type": "FeatureCollection", "features": []}"""
                    }
                    GeoJsonData.JsonString(json)
                }

                val destinationSource = rememberGeoJsonSource(data = destinationFeatureCollection)
                CircleLayer(
                    id = "destination-location",
                    source = destinationSource,
                    color = const(MaterialTheme.colorScheme.error),
                    radius = const(6.dp),
                    strokeColor = const(MaterialTheme.colorScheme.onError),
                    strokeWidth = const(3.dp),
                )

                val routeSource = rememberGeoJsonSource(
                    data = uiState.routeGeoJson ?: GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")
                )
                LineLayer(
                    id = "route-line",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    width = const(5.dp),
                )

                val connectorDotsData = remember(
                    uiState.routeGeoJson,
                    uiState.origin,
                    uiState.destination,
                    uiState.routeStart,
                    uiState.routeEnd,
                ) {
                    if (uiState.routeGeoJson == null) {
                        GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")
                    } else {
                        buildConnectorDotsGeoJson(
                            currentLocation = uiState.origin,
                            routeStart = uiState.routeStart,
                            routeEnd = uiState.routeEnd,
                            destination = uiState.destination,
                        )
                    }
                }
                val connectorDotsSource = rememberGeoJsonSource(data = connectorDotsData)
                CircleLayer(
                    id = "route-connector-dots",
                    source = connectorDotsSource,
                    color = const(MaterialTheme.colorScheme.onSurfaceVariant),
                    radius = const(2.3.dp),
                    opacity = const(0.85f),
                )
            }

            if (!navMode) {
                SearchScaffold(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = if (active) 0.dp else 2.dp,
                            start = if (active) 0.dp else 7.dp,
                            end = if (active) 0.dp else 7.dp,
                        ),
                    query = query,
                    active = active,
                    onQueryChange = { query = it },
                    onActiveChange = { active = it },
                    onResultClick = { selected ->
                        query = selected
                        active = false
                    },
                )
            }

            fun onDirClick() {
                viewModel.requestDirections()
                navMode = true
            }

            fun onClose() {
                viewModel.clearRoute()
                navMode = false
                showTripSummary = false
            }

            if (showTripSummary && !navMode) {
                TripSummaryCard(
                    state = uiState,
                    onRetry = viewModel::retryRoute,
                    onDirectionsClick = { onDirClick() },
                    onClearRoute = { onClose() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(all=0.dp),
                    warningCount = 0,
                )
            }
            val scope = rememberCoroutineScope()
            if (navMode) {
                NavigationModeHeader(
                    instruction = uiState.steps.getOrNull(uiState.currentStepIndex)?.instruction ?: "Continue on route",
                    onExitNavigation = {
                        navMode = false
                        scope.launch {
                            resetCam(cameraState)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                )

                NavigationModeFooter(
                    remainingDistanceMeters = uiState.remainingDistanceMeters ?: uiState.distanceMeters,
                    remainingDurationSeconds = uiState.remainingDurationSeconds ?: uiState.durationSeconds,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp, start = 0.dp, end = 0.dp),
                    onExitNavigation = {
                        navMode = false
                        scope.launch {
                            resetCam(cameraState)
                        }
                    },
                )
            }

        }
    }
}

private fun buildConnectorDotsGeoJson(
    currentLocation: Position?,
    routeStart: Position?,
    routeEnd: Position?,
    destination: Position?,
): GeoJsonData {
    val dots = buildList {
        addAll(generateDotPositions(currentLocation, routeStart))
        addAll(generateDotPositions(routeEnd, destination))
    }

    if (dots.isEmpty()) {
        return GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")
    }

    val features = dots.joinToString(",") { point ->
        """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [${point.longitude}, ${point.latitude}]
          },
          "properties": {}
        }
        """.trimIndent()
    }

    return GeoJsonData.JsonString(
        """
        {
          "type": "FeatureCollection",
          "features": [$features]
        }
        """.trimIndent(),
    )
}

private fun generateDotPositions(
    start: Position?,
    end: Position?,
    spacingMeters: Double = 8.0,
): List<Position> {
    if (start == null || end == null) return emptyList()

    val distanceMeters = approximateDistanceMeters(start, end)
    if (distanceMeters < spacingMeters) return emptyList()

    val steps = ceil(distanceMeters / spacingMeters).toInt()
    return (1 until steps).map { step ->
        val t = step.toDouble() / steps.toDouble()
        Position(
            longitude = start.longitude + ((end.longitude - start.longitude) * t),
            latitude = start.latitude + ((end.latitude - start.latitude) * t),
        )
    }
}

private fun approximateDistanceMeters(start: Position, end: Position): Double {
    val latMeters = (end.latitude - start.latitude) * 111_320.0
    val lonMeters =
        (end.longitude - start.longitude) * 111_320.0 * kotlin.math.cos(Math.toRadians((start.latitude + end.latitude) / 2.0))
    return sqrt((latMeters * latMeters) + (lonMeters * lonMeters))
}
