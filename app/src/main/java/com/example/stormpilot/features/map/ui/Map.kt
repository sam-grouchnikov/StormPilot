package com.example.stormpilot.features.map.ui

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollFieldDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.data.MapsViewModel
import com.example.stormpilot.features.auth.ui.coloredShadow
import com.example.stormpilot.ui.theme.ExtendedColors
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.value.RasterResampling
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.spatialk.geojson.Position
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsPage(
    viewModel: MapsViewModel = hiltViewModel(),
    onDestinationSelectedStateChanged: (Boolean) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var navMode by remember { mutableStateOf(false) }
    var navigationCameraTrackingEnabled by remember { mutableStateOf(false) }
    var isProgrammaticCameraUpdate by remember { mutableStateOf(false) }
    var is2dNavView by remember { mutableStateOf(false) }
    var radarRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var alertsRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var radarProduct by remember { mutableStateOf(RadarProduct.Reflectivity) }
    var radarFrameCount by remember { mutableStateOf(7) }
    var radarFrameIndex by remember { mutableStateOf(6) }
    var radarReplayPlaying by remember { mutableStateOf(false) }

    val colors = ExtendedColors()

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

    val styleState = rememberStyleState()


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

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { feature ->
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(target = feature.geometry, zoom = 14.0),
                duration = 1.seconds,
            )
        }
    }

    var active by remember { mutableStateOf(false) }
    var showTripSummary by remember { mutableStateOf(false) }
    var showRadarOverlay by remember { mutableStateOf(false) }
    var showSevereAlertsOverlay by remember { mutableStateOf(false) }
    val radarOverlayOpacity = if (showRadarOverlay) 0.75f else 0f
    val radarFrames = remember(radarFrameCount) { radarFrameOffsets(radarFrameCount) }
    val allRadarFrames = remember { radarFrameOffsets(RADAR_MAX_FRAME_COUNT) }
    val selectedRadarFrameSlot = (
        allRadarFrames.size - radarFrames.size + radarFrameIndex
    ).coerceIn(allRadarFrames.indices)
    val selectedRadarFrameOffset = radarFrames.getOrElse(radarFrameIndex) { 0 }
    val radarSite = remember(uiState.origin, cameraState.position.target) {
        nearestNexradSite(uiState.origin ?: cameraState.position.target)
    }
    val radarTileUrls = remember(radarProduct, allRadarFrames, radarRefreshKey, radarSite) {
        allRadarFrames.map { frameOffsetMinutes ->
            radarTileUrl(
                product = radarProduct,
                frameOffsetMinutes = frameOffsetMinutes,
                radarSite = radarSite,
                refreshKey = radarRefreshKey,
            )
        }
    }
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

    LaunchedEffect(showRadarOverlay, radarReplayPlaying, radarFrames) {
        if (showRadarOverlay && radarReplayPlaying) {
            while (true) {
                delay(RADAR_REPLAY_FRAME_DELAY_MS)
                radarFrameIndex = (radarFrameIndex + 1) % radarFrames.size
            }
        }
    }

    LaunchedEffect(showRadarOverlay) {
        if (!showRadarOverlay) {
            radarReplayPlaying = false
            radarFrameIndex = radarFrames.lastIndex
        }
    }

    LaunchedEffect(radarFrameCount) {
        radarFrameIndex = radarFrames.lastIndex
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
        opaqueNavigationBar = false,
    ) {
        val searchContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val mapStyle = if (AppSettings.isDarkMode) {
            "asset://map-dark.json"
        } else {
            "asset://map-light.json"
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
                    color = const(colors.routingLine),
                    width = const(if (navMode) 12.dp else 5.dp)
                )

                radarTileUrls.forEachIndexed { frameIndex, radarTileUrl ->
                    val radarSource = rememberRasterSource(
                        tiles = listOf(radarTileUrl),
                        options = TileSetOptions(
                            minZoom = 1,
                            maxZoom = 12,
                        ),
                        tileSize = 256,
                    )
                    RasterLayer(
                        id = "radar-overlay-layer-$frameIndex",
                        source = radarSource,
                        opacity = const(
                            if (frameIndex == selectedRadarFrameSlot) {
                                radarOverlayOpacity
                            } else {
                                0f
                            },
                        ),
                        resampling = const(RasterResampling.Linear),
                        fadeDuration = const(RADAR_TILE_FADE_DURATION),
                    )
                }

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
                    .align(Alignment.BottomCenter),
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
                visible = showRadarOverlay && !active,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(120)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = when (footerState) {
                            MapsFooterState.Navigation -> 124.dp
                            MapsFooterState.Summary -> 174.dp
                            MapsFooterState.Hidden -> 84.dp
                        },
                    ),
            ) {
                RadarControlsPopup(
                    product = radarProduct,
                    frameCount = radarFrameCount,
                    frameOffsetMinutes = selectedRadarFrameOffset,
                    isPlaying = radarReplayPlaying,
                    radarSite = radarSite,
                    onProductChange = {
                        radarProduct = it
                        radarReplayPlaying = false
                        radarFrameIndex = radarFrames.lastIndex
                    },
                    onFrameCountChange = {
                        radarFrameCount = it
                        radarReplayPlaying = false
                    },
                    onPlayPause = {
                        radarReplayPlaying = !radarReplayPlaying
                    },
                )
            }

            val colors = ExtendedColors()
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
                        modifier = Modifier.coloredShadow(
                            color = colors.purpleShadow,
                            blurRadius = 2.dp
                        )
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
                        modifier = Modifier.coloredShadow(
                            color = colors.purpleShadow,
                            blurRadius = 2.dp
                        )
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
                                modifier = Modifier.coloredShadow(
                                    color = colors.purpleShadow,
                                    blurRadius = 2.dp
                                )
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
                                modifier = Modifier.coloredShadow(
                                    color = colors.purpleShadow,
                                    blurRadius = 2.dp
                                )
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
                    query = searchQuery,
                    active = active,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onActiveChange = { active = it },
                    onSearchSubmit = {
                        viewModel.onSearchSubmitted()
                        showTripSummary = true
                        active = false
                    },
                    onResultClick = { selected ->
                        viewModel.onLocationSelected(selected)
                        showTripSummary = true
                        active = false
                    },
                    onOpenSettings = onOpenSettings,
                    containerColor = searchContainerColor,
                )
            }
        }
    }
}

private val RADAR_TILE_FADE_DURATION = 1.milliseconds
private const val RADAR_REPLAY_FRAME_DELAY_MS = 650L
private const val RADAR_MAX_FRAME_COUNT = 30
private const val RADAR_NATIVE_ZOOM = 6

private enum class RadarProduct(
    val displayName: String,
    val nationalLayer: String,
    val siteProductCode: String,
) {
    Reflectivity(
        displayName = "Reflectivity",
        nationalLayer = "nexrad-n0q",
        siteProductCode = "N0Q",
    ),
    Velocity(
        displayName = "Velocity",
        nationalLayer = "",
        siteProductCode = "N0U",
    ),
}

@Composable
private fun RadarControlsPopup(
    product: RadarProduct,
    frameCount: Int,
    frameOffsetMinutes: Int,
    isPlaying: Boolean,
    radarSite: NexradSite,
    onProductChange: (RadarProduct) -> Unit,
    onFrameCountChange: (Int) -> Unit,
    onPlayPause: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadarProduct.entries.forEach { option ->
                    FilterChip(
                        selected = product == option,
                        onClick = { onProductChange(option) },
                        label = { Text(option.displayName) },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (frameOffsetMinutes == 0) "Now" else "${frameOffsetMinutes}m ago",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf(7, 14, 21, 30).forEach { count ->
                    TextButton(
                        onClick = { onFrameCountChange(count) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (frameCount == count) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                            contentColor = if (frameCount == count) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("${count}f")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (product == RadarProduct.Velocity) {
                    Text(
                        text = radarSite.id,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause radar replay" else "Play radar replay",
                    )
                }
            }
        }
    }
}

private fun radarFrameOffsets(frameCount: Int): List<Int> {
    return (frameCount - 1 downTo 0).map { it * 5 }
}

private fun radarTileUrl(
    product: RadarProduct,
    frameOffsetMinutes: Int,
    radarSite: NexradSite,
    refreshKey: Long,
): String {
    val layer = when (product) {
        RadarProduct.Reflectivity -> if (frameOffsetMinutes == 0) {
            "${product.nationalLayer}-900913"
        } else {
            "ridge::USCOMP-${product.siteProductCode}-${radarArchiveTimestamp(refreshKey, frameOffsetMinutes)}"
        }
        RadarProduct.Velocity -> {
            val frameSuffix = if (frameOffsetMinutes == 0) {
                "900913"
            } else {
                "900913-m${frameOffsetMinutes.coerceAtMost(50).toString().padStart(2, '0')}m"
            }
            "ridge::${radarSite.id.removePrefix("K")}-${product.siteProductCode}-$frameSuffix"
        }
    }
    return "https://mesonet.agron.iastate.edu/cache/tile.py/1.0.0/$layer/{z}/{x}/{y}.png?v=$refreshKey"
}

private fun radarArchiveTimestamp(refreshKey: Long, frameOffsetMinutes: Int): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = refreshKey - frameOffsetMinutes * 60_000L
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.MINUTE, get(Calendar.MINUTE) - get(Calendar.MINUTE) % 5)
    }
    val year = calendar.get(Calendar.YEAR).toString().padStart(4, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
    return "$year$month$day$hour$minute"
}

private data class NexradSite(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

private fun nearestNexradSite(position: Position): NexradSite {
    return NexradSites.minBy { site ->
        val latDelta = site.latitude - position.latitude
        val lonDelta = site.longitude - position.longitude
        latDelta * latDelta + lonDelta * lonDelta
    }
}

private val NexradSites = listOf(
    NexradSite("KABR", 45.46, -98.41),
    NexradSite("KABX", 35.15, -106.82),
    NexradSite("KAKQ", 36.98, -77.01),
    NexradSite("KAMA", 35.23, -101.71),
    NexradSite("KAMX", 25.61, -80.41),
    NexradSite("KAPX", 44.91, -84.72),
    NexradSite("KARX", 43.82, -91.19),
    NexradSite("KATX", 48.19, -122.50),
    NexradSite("KBBX", 39.50, -121.63),
    NexradSite("KBGM", 42.20, -75.98),
    NexradSite("KBHX", 40.50, -124.29),
    NexradSite("KBIS", 46.77, -100.76),
    NexradSite("KBLX", 45.85, -108.61),
    NexradSite("KBMX", 33.17, -86.77),
    NexradSite("KBOX", 41.96, -71.14),
    NexradSite("KBRO", 25.92, -97.42),
    NexradSite("KBUF", 42.95, -78.74),
    NexradSite("KBYX", 24.60, -81.70),
    NexradSite("KCAE", 33.95, -81.12),
    NexradSite("KCBW", 46.04, -67.81),
    NexradSite("KCBX", 43.49, -116.24),
    NexradSite("KCCX", 40.92, -78.00),
    NexradSite("KCLE", 41.41, -81.86),
    NexradSite("KCLX", 32.66, -81.04),
    NexradSite("KCRP", 27.78, -97.51),
    NexradSite("KCXX", 44.51, -73.17),
    NexradSite("KCYS", 41.15, -104.81),
    NexradSite("KDAX", 38.50, -121.68),
    NexradSite("KDDC", 37.76, -99.97),
    NexradSite("KDFX", 29.27, -100.28),
    NexradSite("KDGX", 32.28, -89.98),
    NexradSite("KDIX", 39.95, -74.41),
    NexradSite("KDLH", 46.84, -92.21),
    NexradSite("KDMX", 41.73, -93.72),
    NexradSite("KDOX", 38.83, -75.44),
    NexradSite("KDTX", 42.70, -83.47),
    NexradSite("KDVN", 41.61, -90.58),
    NexradSite("KDYX", 32.54, -99.25),
    NexradSite("KEAX", 38.81, -94.26),
    NexradSite("KEMX", 31.89, -110.63),
    NexradSite("KENX", 42.59, -74.06),
    NexradSite("KEOX", 31.46, -85.46),
    NexradSite("KEPZ", 31.87, -106.70),
    NexradSite("KESX", 35.70, -114.89),
    NexradSite("KEVX", 30.56, -85.92),
    NexradSite("KEWX", 29.70, -98.03),
    NexradSite("KEYX", 35.10, -117.56),
    NexradSite("KFCX", 37.02, -80.27),
    NexradSite("KFDR", 34.36, -98.98),
    NexradSite("KFDX", 34.63, -103.63),
    NexradSite("KFFC", 33.36, -84.57),
    NexradSite("KFSD", 43.59, -96.73),
    NexradSite("KFSX", 34.57, -111.20),
    NexradSite("KFTG", 39.79, -104.55),
    NexradSite("KFWS", 32.57, -97.30),
    NexradSite("KGGW", 48.21, -106.63),
    NexradSite("KGJX", 39.06, -108.21),
    NexradSite("KGLD", 39.37, -101.70),
    NexradSite("KGRB", 44.50, -88.11),
    NexradSite("KGRK", 30.72, -97.38),
    NexradSite("KGRR", 42.89, -85.54),
    NexradSite("KGSP", 34.88, -82.22),
    NexradSite("KGWX", 33.90, -88.33),
    NexradSite("KGYX", 43.89, -70.26),
    NexradSite("KHDX", 33.08, -106.12),
    NexradSite("KHGX", 29.47, -95.08),
    NexradSite("KHNX", 36.31, -119.63),
    NexradSite("KHPX", 36.74, -87.29),
    NexradSite("KHTX", 34.93, -86.08),
    NexradSite("KICT", 37.65, -97.44),
    NexradSite("KILN", 39.42, -83.82),
    NexradSite("KILX", 40.15, -89.34),
    NexradSite("KIND", 39.71, -86.28),
    NexradSite("KINX", 36.18, -95.56),
    NexradSite("KIWA", 33.29, -111.67),
    NexradSite("KIWX", 41.36, -85.70),
    NexradSite("KJAX", 30.48, -81.70),
    NexradSite("KJGX", 32.68, -83.35),
    NexradSite("KJKL", 37.59, -83.31),
    NexradSite("KLBB", 33.65, -101.81),
    NexradSite("KLCH", 30.13, -93.22),
    NexradSite("KLGX", 47.12, -124.11),
    NexradSite("KLIX", 30.34, -89.83),
    NexradSite("KLNX", 41.96, -100.58),
    NexradSite("KLOT", 41.60, -88.08),
    NexradSite("KLRX", 40.74, -116.80),
    NexradSite("KLSX", 38.70, -90.68),
    NexradSite("KLTX", 33.99, -78.43),
    NexradSite("KLVX", 37.98, -85.94),
    NexradSite("KLWX", 38.98, -77.48),
    NexradSite("KLZK", 34.84, -92.26),
    NexradSite("KMAF", 31.94, -102.19),
    NexradSite("KMAX", 42.08, -122.72),
    NexradSite("KMBX", 48.39, -100.86),
    NexradSite("KMHX", 34.78, -76.88),
    NexradSite("KMKX", 42.97, -88.55),
    NexradSite("KMLB", 28.11, -80.65),
    NexradSite("KMOB", 30.68, -88.24),
    NexradSite("KMPX", 44.85, -93.57),
    NexradSite("KMQT", 46.53, -87.55),
    NexradSite("KMRX", 36.17, -83.40),
    NexradSite("KMSX", 47.04, -113.99),
    NexradSite("KMTX", 41.26, -112.45),
    NexradSite("KMUX", 37.16, -121.90),
    NexradSite("KMVX", 47.53, -97.33),
    NexradSite("KMXX", 32.54, -85.79),
    NexradSite("KNKX", 32.92, -117.04),
    NexradSite("KNQA", 35.34, -89.87),
    NexradSite("KOAX", 41.32, -96.37),
    NexradSite("KOHX", 36.25, -86.56),
    NexradSite("KOKX", 40.87, -72.86),
    NexradSite("KOTX", 47.68, -117.63),
    NexradSite("KPAH", 37.07, -88.77),
    NexradSite("KPBZ", 40.53, -80.22),
    NexradSite("KPDT", 45.69, -118.85),
    NexradSite("KPOE", 31.16, -92.98),
    NexradSite("KPUX", 38.46, -104.18),
    NexradSite("KRAX", 35.67, -78.49),
    NexradSite("KRGX", 39.75, -119.46),
    NexradSite("KRIW", 43.07, -108.48),
    NexradSite("KRLX", 38.31, -81.72),
    NexradSite("KRTX", 45.71, -122.97),
    NexradSite("KSFX", 43.11, -112.69),
    NexradSite("KSGF", 37.24, -93.40),
    NexradSite("KSHV", 32.45, -93.84),
    NexradSite("KSJT", 31.37, -100.49),
    NexradSite("KSOX", 33.82, -117.64),
    NexradSite("KSRX", 35.29, -94.36),
    NexradSite("KTBW", 27.71, -82.40),
    NexradSite("KTFX", 47.46, -111.39),
    NexradSite("KTLH", 30.40, -84.33),
    NexradSite("KTLX", 35.33, -97.28),
    NexradSite("KTWX", 38.99, -96.23),
    NexradSite("KTYX", 43.76, -75.68),
    NexradSite("KUDX", 44.13, -102.83),
    NexradSite("KUEX", 40.32, -98.44),
    NexradSite("KVAX", 30.89, -83.00),
    NexradSite("KVBX", 34.84, -120.40),
    NexradSite("KVNX", 36.74, -98.13),
    NexradSite("KVTX", 34.41, -119.18),
    NexradSite("KVWX", 38.26, -87.72),
    NexradSite("KYUX", 32.50, -114.66),
)
