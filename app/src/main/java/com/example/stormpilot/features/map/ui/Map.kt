package com.example.stormpilot.features.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.isAppInDarkMode
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertDetailBottomSheet
import com.example.stormpilot.features.map.ui.navigation.NavigationModeFooter
import com.example.stormpilot.features.map.ui.navigation.NavigationModeHeader
import com.example.stormpilot.features.map.ui.navigation.NavigationRecenterButton
import com.example.stormpilot.features.map.ui.navigation.TripSummaryCard
import com.example.stormpilot.features.map.ui.radar.NexradSite
import com.example.stormpilot.features.map.ui.radar.RADAR_LOG_TAG
import com.example.stormpilot.features.map.ui.radar.RADAR_PLAYBACK_FRAME_COUNTS
import com.example.stormpilot.features.map.ui.radar.RADAR_PLAYBACK_FRAME_DELAY_MS
import com.example.stormpilot.features.map.ui.radar.RADAR_PLAYBACK_RESTART_PAUSE_MS
import com.example.stormpilot.features.map.ui.radar.RADAR_TILE_WARMUP_POLL_INTERVAL_MS
import com.example.stormpilot.features.map.ui.radar.RadarProduct
import com.example.stormpilot.features.map.ui.radar.RadarRasterLayers
import com.example.stormpilot.features.map.ui.radar.RadarSiteLayers
import com.example.stormpilot.features.map.ui.radar.RadarStatusPopup
import com.example.stormpilot.features.map.ui.radar.RadarTileMetadata
import com.example.stormpilot.features.map.ui.radar.RadarTileMetadataKey
import com.example.stormpilot.features.map.ui.radar.fetchRadarTileMetadata
import com.example.stormpilot.features.map.ui.radar.radarScanEvents
import com.example.stormpilot.features.map.ui.search.SearchScaffold
import com.example.stormpilot.features.map.ui.search.SearchResultsBottomSheet
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.features.shared.viewmodels.MapsFooterState
import com.example.stormpilot.features.shared.viewmodels.MapsViewModel
import com.example.stormpilot.features.shared.viewmodels.navigationInstruction
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.layers.Anchor
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
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
    onOpenStormAiChat: () -> Unit = {},
) {
    val context = LocalContext.current
    val rootView = LocalView.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val mapSearchResults by viewModel.mapSearchResults.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var navMode by remember { mutableStateOf(false) }
    var navigationCameraTrackingEnabled by remember { mutableStateOf(false) }
    var isProgrammaticCameraUpdate by remember { mutableStateOf(false) }
    var programmaticCameraUpdateGeneration by remember { mutableLongStateOf(0L) }
    var is2dNavView by remember { mutableStateOf(false) }
    var radarRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedRadarSite by remember { mutableStateOf<NexradSite?>(null) }
    var selectedRadarProduct by remember { mutableStateOf(RadarProduct.REFLECTIVITY) }
    var radarPlaybackFrameCount by remember { mutableStateOf(RADAR_PLAYBACK_FRAME_COUNTS.first()) }
    var radarPlaybackChangesAgo by remember { mutableStateOf<Int?>(null) }
    var isRadarPlaybackRunning by remember { mutableStateOf(false) }
    var radarTileMetadata by remember { mutableStateOf<RadarTileMetadata?>(null) }
    val radarTileMetadataByKey = remember { mutableStateMapOf<RadarTileMetadataKey, RadarTileMetadata>() }
    val radarTileRefreshKeyByMetadataKey = remember { mutableStateMapOf<RadarTileMetadataKey, Long>() }
    var radarTileLoadingKey by remember { mutableStateOf<RadarTileMetadataKey?>(null) }
    var radarTileError by remember { mutableStateOf<String?>(null) }
    var lastMapTapPosition by remember { mutableStateOf<Position?>(null) }
    var focusedSearchResult by remember { mutableStateOf<PhotonFeature?>(null) }
    val isDarkMode = isAppInDarkMode()

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

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(rootView, mapSearchResults, focusedSearchResult) {
        repeat(8) {
            if (disableMapLibreFocusOverlay(rootView)) return@LaunchedEffect
            delay(50)
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
                    .setMinUpdateIntervalMillis(500L)
                    .build()

            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location ->
                            viewModel.onUserLocationUpdated(
                                position = Position(location.longitude, location.latitude),
                                bearingDegrees = location.bearing.takeIf { location.hasBearing() }?.toDouble(),
                            )
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

    LaunchedEffect(
        navMode,
        navigationCameraTrackingEnabled,
        uiState.origin,
        uiState.navigationBearingDegrees,
        is2dNavView,
    ) {
        if (navMode && navigationCameraTrackingEnabled && uiState.origin != null) {
            val origin = uiState.origin!!
            val bearing = uiState.navigationBearingDegrees ?: cameraState.position.bearing
            val updateGeneration = programmaticCameraUpdateGeneration + 1L
            programmaticCameraUpdateGeneration = updateGeneration

            try {
                isProgrammaticCameraUpdate = true
                cameraState.animateTo(
                    finalPosition = cameraState.position.copy(
                        target = origin,
                        zoom = 17.5,
                        tilt = if (is2dNavView) 0.0 else 50.0,
                        bearing = bearing,
                    ),
                    duration = 650.milliseconds,
                )
            } finally {
                withContext(NonCancellable) {
                    delay(50)
                    if (programmaticCameraUpdateGeneration == updateGeneration) {
                        isProgrammaticCameraUpdate = false
                    }
                }
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

    LaunchedEffect(focusedSearchResult, mapSearchResults) {
        val focusedResult = focusedSearchResult
            ?.takeIf { focused -> mapSearchResults.take(5).any { it == focused } }
        focusedResult?.let { feature ->
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(target = feature.geometry, zoom = 15.0),
                duration = 700.milliseconds,
            )
        }
    }

    var active by remember { mutableStateOf(false) }
    var showTripSummary by remember { mutableStateOf(false) }
    var showRadarOverlay by remember { mutableStateOf(false) }
    var showSevereAlertsOverlay by remember { mutableStateOf(false) }
    val selectedRadarKey = selectedRadarSite
        ?.takeIf { showRadarOverlay }
        ?.let { site ->
            RadarTileMetadataKey(
                site = site.id,
                product = selectedRadarProduct,
                changesAgo = radarPlaybackChangesAgo,
            )
        }
    val selectedRadarMetadata = selectedRadarSite
        ?.takeIf { showRadarOverlay }
        ?.let {
            selectedRadarKey?.let { key ->
                radarTileMetadataByKey[key]
            } ?: radarTileMetadata
        }
        ?.takeIf { metadata ->
            metadata.key == selectedRadarKey
        }
    val activeRadarMetadata = selectedRadarMetadata
        ?.takeIf { metadata -> metadata.tilesReady }
    val playbackRadarMetadata = selectedRadarSite
        ?.takeIf { showRadarOverlay && isRadarPlaybackRunning }
        ?.let { site ->
            (radarPlaybackFrameCount downTo 0).mapNotNull { changesAgo ->
                val key = RadarTileMetadataKey(
                    site = site.id,
                    product = selectedRadarProduct,
                    changesAgo = changesAgo,
                )
                radarTileMetadataByKey[key]
                    ?.takeIf { metadata ->
                        metadata.tilesReady
                    }
            }
        }
        .orEmpty()
    val severeAlertsOverlayOpacity = if (showSevereAlertsOverlay) 0.85f else 0f
    val footerState = when {
        navMode -> MapsFooterState.Navigation
        showTripSummary -> MapsFooterState.Summary
        else -> MapsFooterState.Hidden
    }

    LaunchedEffect(uiState.showAlertsOverlay) {
        if (uiState.showAlertsOverlay) {
            showSevereAlertsOverlay = true
        }
    }

    LaunchedEffect(uiState.assistantFocusPosition) {
        uiState.assistantFocusPosition?.let { position ->
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(target = position, zoom = 11.0),
                duration = 1.seconds,
            )
        }
    }

    LaunchedEffect(mapSearchResults) {
        val highlightedResults = mapSearchResults.take(5)
        focusedSearchResult = highlightedResults.firstOrNull()
        if (highlightedResults.isNotEmpty()) {
            showTripSummary = false
            navMode = false
            navigationCameraTrackingEnabled = false
        }
    }

    LaunchedEffect(uiState.destination, mapSearchResults) {
        if (!navMode && uiState.destination != null && mapSearchResults.isEmpty()) {
            showTripSummary = true
        }
    }


    LaunchedEffect(showRadarOverlay, selectedRadarSite, selectedRadarProduct) {
        val site = selectedRadarSite
        if (!showRadarOverlay || site == null) {
            return@LaunchedEffect
        }

        radarScanEvents(site.id, selectedRadarProduct).collect { event ->
            Log.d(
                RADAR_LOG_TAG,
                "Radar scan event site=${event.site}, product=${event.product.pathSegment}, " +
                        "scan=${event.scanTimeUtc}, id=${event.scanId}",
            )
            radarRefreshKey = max(radarRefreshKey + 1L, System.currentTimeMillis())
        }
    }

    LaunchedEffect(showSevereAlertsOverlay) {
        if (showSevereAlertsOverlay) {
            viewModel.refreshAlertPolygons()
        }
    }

    LaunchedEffect(
        showRadarOverlay,
        selectedRadarSite,
        selectedRadarProduct,
        radarPlaybackFrameCount,
        isRadarPlaybackRunning,
    ) {
        val site = selectedRadarSite
        if (!showRadarOverlay || site == null || !isRadarPlaybackRunning) {
            return@LaunchedEffect
        }

        while (true) {
            val playbackFrameKeys = (radarPlaybackFrameCount downTo 0).map { changesAgo ->
                RadarTileMetadataKey(
                    site = site.id,
                    product = selectedRadarProduct,
                    changesAgo = changesAgo,
                )
            }
            val arePlaybackFramesReady = playbackFrameKeys.all { key ->
                radarTileMetadataByKey[key]
                    ?.tilesReady == true
            }

            if (!arePlaybackFramesReady) {
                delay(RADAR_TILE_WARMUP_POLL_INTERVAL_MS)
                continue
            }

            val currentChangesAgo = radarPlaybackChangesAgo
            if (currentChangesAgo == null || currentChangesAgo !in 0..radarPlaybackFrameCount) {
                radarPlaybackChangesAgo = radarPlaybackFrameCount
                continue
            }
            val currentKey = RadarTileMetadataKey(
                site = site.id,
                product = selectedRadarProduct,
                changesAgo = currentChangesAgo,
            )
            val nextChangesAgo = if (currentChangesAgo <= 0) {
                radarPlaybackFrameCount
            } else {
                currentChangesAgo - 1
            }
            val nextKey = RadarTileMetadataKey(
                site = site.id,
                product = selectedRadarProduct,
                changesAgo = nextChangesAgo,
            )

            val currentMetadata = radarTileMetadataByKey[currentKey]
            val nextMetadata = radarTileMetadataByKey[nextKey]
            if (currentMetadata?.tilesReady == true && nextMetadata?.tilesReady == true) {
                val delayBeforeNextFrame = RADAR_PLAYBACK_FRAME_DELAY_MS +
                    if (nextChangesAgo == radarPlaybackFrameCount) {
                        RADAR_PLAYBACK_RESTART_PAUSE_MS
                    } else {
                        0L
                    }
                delay(delayBeforeNextFrame)
                radarPlaybackChangesAgo = nextChangesAgo
            } else {
                delay(RADAR_TILE_WARMUP_POLL_INTERVAL_MS)
            }
        }
    }

    LaunchedEffect(
        showRadarOverlay,
        selectedRadarSite,
        selectedRadarProduct,
        radarPlaybackFrameCount,
        isRadarPlaybackRunning,
        radarRefreshKey,
    ) {
        val site = selectedRadarSite
        if (!showRadarOverlay || site == null || !isRadarPlaybackRunning) {
            return@LaunchedEffect
        }

        val product = selectedRadarProduct
        for (changesAgo in radarPlaybackFrameCount downTo 0) {
            val metadataKey = RadarTileMetadataKey(site.id, product, changesAgo)
            val cachedMetadata = radarTileMetadataByKey[metadataKey]
                ?.takeIf { radarTileRefreshKeyByMetadataKey[metadataKey] == radarRefreshKey }
            if (cachedMetadata?.tilesReady == true) {
                continue
            }

            try {
                do {
                    Log.d(
                        RADAR_LOG_TAG,
                        "Prewarming radar playback metadata for site=${site.id}, " +
                                "product=${product.pathSegment}, changesAgo=$changesAgo",
                    )
                    val metadata = fetchRadarTileMetadata(site.id, product, changesAgo)
                    if (metadata.tilesReady) {
                        radarTileMetadataByKey[metadata.key] = metadata
                        radarTileRefreshKeyByMetadataKey[metadata.key] = radarRefreshKey
                    } else {
                        delay(RADAR_TILE_WARMUP_POLL_INTERVAL_MS)
                    }
                } while (!metadata.tilesReady)
            } catch (e: Exception) {
                Log.e(
                    RADAR_LOG_TAG,
                    "Radar playback metadata unavailable for site=${site.id}, " +
                            "product=${product.pathSegment}, changesAgo=$changesAgo",
                    e,
                )
            }
        }
    }

    LaunchedEffect(showRadarOverlay, selectedRadarSite, selectedRadarProduct, radarPlaybackChangesAgo, radarRefreshKey) {
        val site = selectedRadarSite
        if (!showRadarOverlay || site == null) {
            radarTileLoadingKey = null
            radarTileError = null
            return@LaunchedEffect
        }
        val product = selectedRadarProduct
        val metadataKey = RadarTileMetadataKey(
            site = site.id,
            product = product,
            changesAgo = radarPlaybackChangesAgo,
        )

        val cachedMetadata = radarTileMetadataByKey[metadataKey]
            ?.takeIf { radarTileRefreshKeyByMetadataKey[metadataKey] == radarRefreshKey }
        if (cachedMetadata?.tilesReady == true) {
            radarTileMetadata = cachedMetadata
            radarTileLoadingKey = null
        } else if (radarTileMetadata?.key != metadataKey) {
            radarTileMetadata = null
            radarTileLoadingKey = metadataKey
        } else {
            radarTileLoadingKey = metadataKey
        }
        radarTileError = null

        try {
            do {
                Log.d(
                    RADAR_LOG_TAG,
                    "Loading radar metadata for site=${site.id}, product=${product.pathSegment}, " +
                            "changesAgo=${radarPlaybackChangesAgo ?: "latest"}",
                )
                val metadata = fetchRadarTileMetadata(site.id, product, radarPlaybackChangesAgo)
                Log.d(
                    RADAR_LOG_TAG,
                    "Loaded radar metadata site=${metadata.site}, product=${metadata.product.pathSegment}, " +
                            "changesAgo=${metadata.changesAgo ?: "latest"}, " +
                            "scan=${metadata.scanTimeUtc}, " +
                            "ready=${metadata.tilesReady}, warmupStarted=${metadata.warmupStarted}, " +
                            "zoom=${metadata.minZoom}-${metadata.maxZoom}, " +
                            "rasterNativeMaxZoom=${metadata.rasterNativeMaxZoom}, tileSize=${metadata.tileSize}, " +
                            "rasterUrl=${metadata.rasterTileRequestUrl}",
                )
                radarTileMetadata = metadata
                radarTileRefreshKeyByMetadataKey[metadata.key] = radarRefreshKey
                if (metadata.tilesReady) {
                    radarTileMetadataByKey[metadata.key] = metadata
                }
                if (!metadata.tilesReady) {
                    delay(RADAR_TILE_WARMUP_POLL_INTERVAL_MS)
                }
            } while (!metadata.tilesReady)
        } catch (e: Exception) {
            Log.e(
                RADAR_LOG_TAG,
                "Radar metadata unavailable for site=${site.id}, product=${product.pathSegment}, " +
                        "changesAgo=${radarPlaybackChangesAgo ?: "latest"}",
                e,
            )
            radarTileError = e.message ?: "Radar tiles unavailable."
        } finally {
            if (radarTileLoadingKey == metadataKey) {
                radarTileLoadingKey = null
            }
        }
    }

    StormPilotTheme(
        darkTheme = isDarkMode,
        opaqueNavigationBar = false,
    ) {
        val searchContainerColor = searchSurfaceColor(isDarkMode)
        val mapStyle = if (isDarkMode) {
            "asset://map-dark.json"
        } else {
            "asset://map-light.json"
        }


        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                baseStyle = BaseStyle.Uri(mapStyle),
                cameraState = cameraState,
                onMapClick = { point, _ ->
                    lastMapTapPosition = point
                    ClickResult.Pass
                },
                onMapLongClick = { point, _ ->
                    showTripSummary = true
                    viewModel.onDestinationSelected(point)
                    ClickResult.Consume
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusProperties { canFocus = false },
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
                val labelLayerId = if (isDarkMode) "places_locality" else "labels"
                Anchor.Below(labelLayerId) {
                    RadarRasterLayers(
                        activeMetadata = activeRadarMetadata,
                        playbackMetadata = playbackRadarMetadata,
                    )
                    RouteLayer(
                        routeGeoJson = uiState.routeGeoJson,
                        isNavigationMode = navMode,
                        colors = colors,
                    )
                    SevereAlertsLayers(
                        visible = showSevereAlertsOverlay,
                        alertsGeoJson = uiState.alertsGeoJson,
                        opacity = severeAlertsOverlayOpacity,
                        lastMapTapPosition = lastMapTapPosition,
                        onAlertTapped = viewModel::onAlertPolygonTapped,
                    )
                }
                UserLocationLayer(origin = uiState.origin)
                DestinationLayer(destination = uiState.destination)
                RadarSiteLayers(
                    visible = showRadarOverlay,
                    selectedSite = selectedRadarSite,
                    onSiteSelected = { site ->
                        selectedRadarSite = site
                        radarTileError = null
                    },
                )
                SearchResultLayers(
                    mapSearchResults = mapSearchResults,
                    focusedSearchResult = focusedSearchResult,
                )
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

            fun onSafeDirClick() {
                viewModel.requestSafeDirections()
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
                        onSafeDirectionsClick = { onSafeDirClick() },
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
                visible = showRadarOverlay && !active && selectedRadarSite != null,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(120)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = when (footerState) {
                            MapsFooterState.Navigation -> 117.dp
                            MapsFooterState.Summary -> 167.dp
                            MapsFooterState.Hidden -> 77.dp
                        },
                    ),
            ) {
                RadarStatusPopup(
                    site = selectedRadarSite,
                    metadata = selectedRadarMetadata,
                    selectedProduct = selectedRadarProduct,
                    onProductSelected = { product ->
                        selectedRadarProduct = product
                        radarTileError = null
                    },
                    playbackFrameCount = radarPlaybackFrameCount,
                    playbackChangesAgo = radarPlaybackChangesAgo,
                    isPlaybackRunning = isRadarPlaybackRunning,
                    onPlaybackFrameCountSelected = { frameCount ->
                        radarPlaybackFrameCount = frameCount
                        if (radarPlaybackChangesAgo != null) {
                            radarPlaybackChangesAgo = frameCount
                        }
                        radarTileError = null
                    },
                    onPlaybackToggled = {
                        if (isRadarPlaybackRunning) {
                            isRadarPlaybackRunning = false
                        } else {
                            val currentPlaybackChangesAgo = radarPlaybackChangesAgo
                            if (
                                currentPlaybackChangesAgo != null &&
                                currentPlaybackChangesAgo !in 0..radarPlaybackFrameCount
                            ) {
                                radarPlaybackChangesAgo = null
                            }
                            isRadarPlaybackRunning = true
                            radarTileError = null
                        }
                    },
                    isLoading = radarTileLoadingKey == selectedRadarKey,
                    errorMessage = radarTileError,
                )
            }

            AnimatedVisibility(
                visible = !active,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(120)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 13.dp, top = 74.dp),
            ) {
                MapOverlayControls(
                    showRadarOverlay = showRadarOverlay,
                    onRadarOverlayClick = {
                        val willShowRadarOverlay = !showRadarOverlay
                        showRadarOverlay = willShowRadarOverlay
                        if (!willShowRadarOverlay) {
                            isRadarPlaybackRunning = false
                            radarPlaybackChangesAgo = null
                        }
                        Log.d(RADAR_LOG_TAG, "Radar overlay enabled=$showRadarOverlay")
                    },
                    showSevereAlertsOverlay = showSevereAlertsOverlay,
                    onSevereAlertsOverlayClick = {
                        showSevereAlertsOverlay = !showSevereAlertsOverlay
                    },
                    navMode = navMode,
                    is2dNavView = is2dNavView,
                    onNavViewToggleClick = {
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
                    colors = colors,
                )
            }

            AnimatedVisibility(
                visible = navMode && !active,
                enter = fadeIn(tween(300)) + slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(300),
                ),
                exit = fadeOut(tween(200)) + slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(200),
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 12.dp, bottom = 116.dp),
            ) {
                NavigationRecenterButton(
                    onClick = {
                        navigationCameraTrackingEnabled = true
                        is2dNavView = false
                    },
                )
            }

            if (!navMode) {
                SearchScaffold(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxSize(),
                    query = searchQuery,
                    active = active,
                    searchResults = searchResults,
                    recentSearches = recentSearches,
                    isSearching = isSearching,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onActiveChange = { active = it },
                    onSearchSubmit = {
                        viewModel.onSearchSubmitted()
                        showTripSummary = false
                        active = false
                    },
                    onResultClick = { selected ->
                        viewModel.onLocationSelected(selected)
                        showTripSummary = true
                        active = false
                    },
                    onOpenSettings = onOpenSettings,
                    onOpenStormAiChat = onOpenStormAiChat,
                    containerColor = searchContainerColor,
                )
            }

            SearchResultsBottomSheet(
                results = if (!active && !navMode && !showTripSummary) mapSearchResults else emptyList(),
                isDarkMode = isDarkMode,
                containerColor = searchContainerColor,
                onDismiss = {
                    focusedSearchResult = null
                    viewModel.clearMapSearchResults()
                },
                onResultFocused = { focusedSearchResult = it },
                onDirectionsClick = { selected ->
                    viewModel.onLocationSelected(selected)
                    showTripSummary = true
                    active = false
                },
            )

            AlertDetailBottomSheet(
                isVisible = uiState.isAlertDetailVisible,
                alert = uiState.selectedAlert,
                isLoading = uiState.isAlertDetailLoading,
                errorMessage = uiState.alertDetailError,
                onDismiss = viewModel::dismissAlertDetail,
            )

            uiState.stormRouteAlertMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = viewModel::dismissStormRouteAlert,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    title = {
                        Text(text = "Storm warning on route")
                    },
                    text = {
                        Text(text = message)
                    },
                    confirmButton = {
                        TextButton(onClick = viewModel::dismissStormRouteAlert) {
                            Text(text = "OK")
                        }
                    },
                )
            }
        }
    }
}
