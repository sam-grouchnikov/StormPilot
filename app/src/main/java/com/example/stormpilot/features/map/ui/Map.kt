package com.example.stormpilot.features.map.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.data.MapsViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.value.RasterResampling
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
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
    var navigationCameraTrackingEnabled by remember { mutableStateOf(false) }
    var isProgrammaticCameraUpdate by remember { mutableStateOf(false) }
    var is2dNavView by remember { mutableStateOf(false) }
    var radarRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var alertsRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }

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

    LaunchedEffect(navMode, navigationCameraTrackingEnabled, uiState.origin, uiState.currentStepIndex, uiState.destination, is2dNavView) {
        if (navMode && navigationCameraTrackingEnabled && uiState.origin != null) {
            val origin = uiState.origin!!
            val currentStep = uiState.steps.getOrNull(uiState.currentStepIndex)
            val routeTarget = currentStep?.maneuverLocation ?: uiState.destination ?: origin
            val bearing = bearingDegrees(from = origin, to = routeTarget)

            try {
                isProgrammaticCameraUpdate = true
                cameraState.animateTo(
                    finalPosition = cameraState.position.copy(
                        target = origin,
                        zoom = 17.5,
                        tilt = if (is2dNavView) 0.0 else 50.0,
                        bearing = bearing,
                    ),
                    duration = 1.seconds,
                )
            } finally {
                delay(50)
                isProgrammaticCameraUpdate = false
            }
        }
    }

    LaunchedEffect(cameraState, navMode) {
        snapshotFlow { cameraState.position }
            .collect {
                if (navMode && navigationCameraTrackingEnabled && !isProgrammaticCameraUpdate) {
                    navigationCameraTrackingEnabled = false
                }
            }
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
    var showTripSummary by remember { mutableStateOf(false) }
    var showRadarOverlay by remember { mutableStateOf(false) }
    var showSevereAlertsOverlay by remember { mutableStateOf(false) }
    val radarOverlayOpacity = if (showRadarOverlay) 0.75f else 0f
    val severeAlertsOverlayOpacity = if (showSevereAlertsOverlay) 0.85f else 0f
    val footerState = when {
        navMode -> MapsFooterState.Navigation
        showTripSummary -> MapsFooterState.Summary
        else -> MapsFooterState.Hidden
    }

    LaunchedEffect(showRadarOverlay) {
        if (showRadarOverlay) {
            while (true) {
                delay(300_000L)
                radarRefreshKey = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(showSevereAlertsOverlay) {
        if (showSevereAlertsOverlay) {
            while (true) {
                delay(300_000L)
                alertsRefreshKey = System.currentTimeMillis()
            }
        }
    }

    StormPilotTheme(
        opaqueNavigationBar = true,
    ) {
        val searchContainerColor = MaterialTheme.colorScheme.inverseOnSurface
        val mapStyle = if (AppSettings.isDarkMode) {
            "https://api.protomaps.com/styles/v5/black/en.json?key=64a5f0a9c35b4ca1"
        } else {
            "https://api.protomaps.com/styles/v5/white/en.json?key=64a5f0a9c35b4ca1"
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                baseStyle = BaseStyle.Uri(mapStyle),
                cameraState = cameraState,
                onMapLongClick = { point, _ ->
                    showTripSummary = true
                    viewModel.onDestinationSelected(point)
                    ClickResult.Consume
                },
                modifier = Modifier.fillMaxSize(),
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

                val radarSource = rememberRasterSource(
                    tiles = listOf("https://mesonet.agron.iastate.edu/cache/tile.py/1.0.0/nexrad-n0q-900913/{z}/{x}/{y}.png?v=$radarRefreshKey"),
                    tileSize = 256,
                )
                RasterLayer(
                    id = "radar-overlay-layer",
                    source = radarSource,
                    opacity = const(radarOverlayOpacity),
                    resampling = const(RasterResampling.Linear),
                    fadeDuration = const(RADAR_TILE_FADE_DURATION),
                )

                val alertsSource = rememberGeoJsonSource(
                    data = uiState.alertsGeoJson ?: GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")
                )

                if (showSevereAlertsOverlay) {
                    FillLayer(
                        id = "alerts-fill",
                        source = alertsSource,
                        color = switch(
                            condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(
                                0x3DFF0000
                            )
                            )),
                            condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(
                                0x3EFF9F15
                            )
                            )),
                            condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(
                                0x3D00BB00
                            )
                            )),
                            fallback = const(Color.Transparent),
                        ),
                    )
                    LineLayer(
                        id = "alerts-outline",
                        source = alertsSource,
                        color = switch(
                            condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(
                                0x80FF0000
                            )
                            )),
                            condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(
                                0xFFD26D03
                            )
                            )),
                            condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(
                                0x8000FF00
                            )
                            )),
                            fallback = const(Color.Transparent),
                        ),
                        width = const(2.dp),
                    )
                }
            }

            SearchStatusBarBackground(
                active = active && !navMode,
                color = searchContainerColor,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            fun onDirClick() {
                viewModel.requestDirections()
                navMode = true
                is2dNavView = false
                navigationCameraTrackingEnabled = true
            }



            val scope = rememberCoroutineScope()

            fun onClose() {
                viewModel.clearRoute()
                navMode = false
                navigationCameraTrackingEnabled = false
                showTripSummary = false
            }



            AnimatedContent(
                targetState = footerState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 90)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 140))
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                label = "maps_footer_transition",
            ) { activeFooter ->
                when (activeFooter) {
                    MapsFooterState.Summary -> TripSummaryCard(
                        state = uiState,
                        onRetry = viewModel::retryRoute,
                        onDirectionsClick = { onDirClick() },
                        onClearRoute = { onClose() },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    MapsFooterState.Navigation -> NavigationModeFooter(
                        remainingDistanceMeters = uiState.remainingDistanceMeters ?: uiState.distanceMeters,
                        remainingDurationSeconds = uiState.remainingDurationSeconds ?: uiState.durationSeconds,
                        modifier = Modifier.fillMaxWidth(),
                        onExitNavigation = {
                            navMode = false
                            navigationCameraTrackingEnabled = false
                            scope.launch {
                                resetCam(cameraState)
                            }
                        },
                    )

                    MapsFooterState.Hidden -> Spacer(modifier = Modifier.height(0.dp))
                }
            }

            if (navMode) {
                NavigationModeHeader(
                    instruction = navigationInstruction(uiState),
                    onExitNavigation = {
                        navMode = false
                        navigationCameraTrackingEnabled = false
                        scope.launch {
                            resetCam(cameraState)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                )
            }

            AnimatedVisibility(
                visible = !active,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(120)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 9.dp, top = 74.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilledIconButton(
                        onClick = { showRadarOverlay = !showRadarOverlay },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showRadarOverlay) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Toggle radar overlay",
                            tint = if (showRadarOverlay) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    FilledIconButton(
                        onClick = { showSevereAlertsOverlay = !showSevereAlertsOverlay },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showSevereAlertsOverlay) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = "Toggle severe weather alerts overlay",
                            tint = if (showSevereAlertsOverlay) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    AnimatedVisibility(
                        visible = navMode,
                        enter = fadeIn(tween(300)) + slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ),
                        exit = fadeOut(tween(200)) + slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(200)
                        ),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledIconButton(
                                onClick = {
                                    is2dNavView = !is2dNavView
                                    if (!navigationCameraTrackingEnabled) {
                                        scope.launch {
                                            try {
                                                isProgrammaticCameraUpdate = true
                                                cameraState.animateTo(
                                                    finalPosition = cameraState.position.copy(
                                                        tilt = if (is2dNavView) 0.0 else 50.0,
                                                    ),
                                                    duration = 1.seconds,
                                                )
                                            } finally {
                                                delay(50)
                                                isProgrammaticCameraUpdate = false
                                            }
                                        }
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                                ),
                            ) {
                                Icon(
                                    imageVector = if (!is2dNavView) Icons.Outlined.Navigation else Icons.Filled.Crop,
                                    contentDescription = if (!is2dNavView) "Enable 2D view" else "Enable 3D view",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    navigationCameraTrackingEnabled = true
                                    is2dNavView = false
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = "Recenter navigation",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            if (!navMode) {
                SearchScaffold(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxSize(),
                    query = query,
                    active = active,
                    onQueryChange = { query = it },
                    onActiveChange = { active = it },
                    onResultClick = { selected ->
                        query = selected
                        active = false
                    },
                    containerColor = searchContainerColor,
                )
            }
        }
    }
}

private val RADAR_TILE_FADE_DURATION = 1.milliseconds
