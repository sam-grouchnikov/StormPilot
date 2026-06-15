package com.example.stormpilot.features.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.BuildConfig
import com.example.stormpilot.features.dashboard.ui.alerts.components.AlertDetailBottomSheet
import com.example.stormpilot.features.shared.viewmodels.MapsFooterState
import com.example.stormpilot.features.shared.viewmodels.MapsViewModel
import com.example.stormpilot.features.shared.viewmodels.navigationInstruction
import com.example.stormpilot.features.map.ui.navigation.NavigationModeFooter
import com.example.stormpilot.features.map.ui.navigation.NavigationModeHeader
import com.example.stormpilot.features.map.ui.navigation.NavigationRecenterButton
import com.example.stormpilot.features.map.ui.navigation.TripSummaryCard
import com.example.stormpilot.features.map.ui.search.SearchScaffold
import com.example.stormpilot.features.map.ui.search.SearchResultsBottomSheet
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.features.navigation.ui.components.coloredShadow
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.core.isAppInDarkMode
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.value.RasterResampling
import org.maplibre.android.maps.MapView as AndroidMapView
import org.maplibre.spatialk.geojson.Position
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
    var alertsRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
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
                    ?.takeIf { radarTileRefreshKeyByMetadataKey[key] == radarRefreshKey }
            } ?: radarTileMetadata
        }
        ?.takeIf { metadata ->
            metadata.key == selectedRadarKey &&
                    radarTileRefreshKeyByMetadataKey[metadata.key] == radarRefreshKey
        }
    val activeRadarMetadata = selectedRadarMetadata
        ?.takeIf { metadata -> metadata.tilesReady }
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

        val initialPlaybackChangesAgo = radarPlaybackChangesAgo
        if (initialPlaybackChangesAgo == null || initialPlaybackChangesAgo !in 0..radarPlaybackFrameCount) {
            radarPlaybackChangesAgo = radarPlaybackFrameCount
        }

        while (true) {
            val currentChangesAgo = radarPlaybackChangesAgo ?: radarPlaybackFrameCount
            val currentKey = RadarTileMetadataKey(
                site = site.id,
                product = selectedRadarProduct,
                changesAgo = currentChangesAgo,
            )

            val currentMetadata = radarTileMetadataByKey[currentKey]
                ?.takeIf { radarTileRefreshKeyByMetadataKey[currentKey] == radarRefreshKey }
            if (currentMetadata?.tilesReady == true) {
                delay(RADAR_PLAYBACK_FRAME_DELAY_MS)
                radarPlaybackChangesAgo = if (currentChangesAgo <= 0) {
                    radarPlaybackFrameCount
                } else {
                    currentChangesAgo - 1
                }
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
                activeRadarMetadata?.let { metadata ->
                    key(metadata.rasterTileRequestUrl) {
                        val radarSource = rememberRasterSource(
                            tiles = listOf(metadata.rasterTileRequestUrl),
                            options = TileSetOptions(
                                minZoom = metadata.minZoom,
                                maxZoom = metadata.rasterNativeMaxZoom,
                            ),
                            tileSize = metadata.tileSize,
                        )
                        RasterLayer(
                            id = "radar-${metadata.product.pathSegment}-${metadata.layerFrameId}-layer",
                            source = radarSource,
                            opacity = const(0.62f),
                            resampling = const(RasterResampling.Linear),
                            fadeDuration = const(RADAR_TILE_FADE_DURATION),
                        )
                    }
                }

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
                        onClick = { features ->
                            val eventType = features.firstOrNull()
                                ?.properties
                                ?.get("prod_type")
                                ?.jsonPrimitive
                                ?.contentOrNull
                            val tappedPosition = lastMapTapPosition

                            if (eventType != null && tappedPosition != null) {
                                viewModel.onAlertPolygonTapped(tappedPosition, eventType)
                                ClickResult.Consume
                            } else {
                                ClickResult.Pass
                            }
                        },
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
                        onClick = { features ->
                            val eventType = features.firstOrNull()
                                ?.properties
                                ?.get("prod_type")
                                ?.jsonPrimitive
                                ?.contentOrNull
                            val tappedPosition = lastMapTapPosition

                            if (eventType != null && tappedPosition != null) {
                                viewModel.onAlertPolygonTapped(tappedPosition, eventType)
                                ClickResult.Consume
                            } else {
                                ClickResult.Pass
                            }
                        },
                    )
                }

                if (showRadarOverlay) {
                    val radarSitesFeatureCollection = remember {
                        GeoJsonData.JsonString(nexradSitesGeoJson(NexradSites))
                    }
                    val radarSitesSource = rememberGeoJsonSource(data = radarSitesFeatureCollection)
                    CircleLayer(
                        id = "radar-site-markers",
                        source = radarSitesSource,
                        minZoom = 2.0f,
                        color = const(Color(0xFFEAF9FF)),
                        radius = const(7.dp),
                        strokeColor = const(Color(0xFF1D252B)),
                        strokeWidth = const(1.5.dp),
                        onClick = { features ->
                            val siteId = features.firstOrNull()
                                ?.properties
                                ?.get("id")
                                ?.jsonPrimitive
                                ?.contentOrNull
                            val site = siteId?.let { tappedId ->
                                NexradSites.firstOrNull { it.id == tappedId }
                            }

                            if (site != null) {
                                Log.d(
                                    RADAR_LOG_TAG,
                                    "Radar site selected id=${site.id}, lat=${site.latitude}, lon=${site.longitude}",
                                )
                                selectedRadarSite = site
                                radarTileError = null
                                ClickResult.Consume
                            } else {
                                ClickResult.Pass
                            }
                        },
                    )

                    selectedRadarSite?.let { site ->
                        val selectedRadarSiteFeatureCollection = remember(site) {
                            GeoJsonData.JsonString(nexradSitesGeoJson(listOf(site)))
                        }
                        val selectedRadarSiteSource = rememberGeoJsonSource(
                            data = selectedRadarSiteFeatureCollection,
                        )
                        CircleLayer(
                            id = "selected-radar-site-marker",
                            source = selectedRadarSiteSource,
                            minZoom = 2.0f,
                            color = const(MaterialTheme.colorScheme.primary),
                            radius = const(7.dp),
                            strokeColor = const(MaterialTheme.colorScheme.onPrimary),
                            strokeWidth = const(2.dp),
                        )
                    }
                }

                val visibleSearchResults = mapSearchResults.take(5)
                val focusedSearchResultForMarkers = focusedSearchResult
                    ?.takeIf { focused -> visibleSearchResults.any { it == focused } }
                val unfocusedSearchResults = visibleSearchResults.filterNot {
                    it == focusedSearchResultForMarkers
                }
                val searchResultsFeatureCollection = remember(unfocusedSearchResults) {
                    GeoJsonData.JsonString(searchResultsGeoJson(unfocusedSearchResults))
                }
                val searchResultsSource = rememberGeoJsonSource(data = searchResultsFeatureCollection)
                CircleLayer(
                    id = "search-result-marker",
                    source = searchResultsSource,
                    color = const(MaterialTheme.colorScheme.error.copy(alpha = 0.80f)),
                    radius = const(6.dp),
                    strokeColor = const(MaterialTheme.colorScheme.onError.copy(alpha = 0.85f)),
                    strokeWidth = const(2.dp),
                )

                val focusedSearchResultFeatureCollection = remember(focusedSearchResultForMarkers) {
                    GeoJsonData.JsonString(
                        searchResultsGeoJson(listOfNotNull(focusedSearchResultForMarkers)),
                    )
                }
                val focusedSearchResultSource = rememberGeoJsonSource(
                    data = focusedSearchResultFeatureCollection,
                )
                CircleLayer(
                    id = "focused-search-result-marker",
                    source = focusedSearchResultSource,
                    color = const(MaterialTheme.colorScheme.error),
                    radius = const(9.dp),
                    strokeColor = const(MaterialTheme.colorScheme.onError),
                    strokeWidth = const(3.dp),
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
                            MapsFooterState.Navigation -> 124.dp
                            MapsFooterState.Summary -> 174.dp
                            MapsFooterState.Hidden -> 84.dp
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
                                currentPlaybackChangesAgo == null ||
                                currentPlaybackChangesAgo !in 0..radarPlaybackFrameCount
                            ) {
                                radarPlaybackChangesAgo = radarPlaybackFrameCount
                            }
                            isRadarPlaybackRunning = true
                            radarTileError = null
                        }
                    },
                    isLoading = radarTileLoadingKey == selectedRadarKey,
                    errorMessage = radarTileError,
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
                    .padding(end = 13.dp, top = 74.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilledIconButton(
                        onClick = {
                            val willShowRadarOverlay = !showRadarOverlay
                            showRadarOverlay = willShowRadarOverlay
                            if (!willShowRadarOverlay) {
                                isRadarPlaybackRunning = false
                                radarPlaybackChangesAgo = null
                            }
                            Log.d(RADAR_LOG_TAG, "Radar overlay enabled=$showRadarOverlay")
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showRadarOverlay) MaterialTheme.colorScheme.primary
                            else colors.iconButtonColor,
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
                            else colors.iconButtonColor,
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
                                    containerColor = colors.searchBarColor,
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
                        }
                    }
                }
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

private fun searchResultsGeoJson(results: List<PhotonFeature>): String {
    val features = results.take(5).joinToString(",") { result ->
        """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [${result.geometry.longitude}, ${result.geometry.latitude}]
          },
          "properties": {}
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun disableMapLibreFocusOverlay(root: View): Boolean {
    var foundMapView = false
    root.forEachViewInTree { view ->
        if (view is AndroidMapView) {
            foundMapView = true
            view.forEachViewInTree { mapViewChild ->
                mapViewChild.clearFocus()
                mapViewChild.isFocusable = false
                mapViewChild.isFocusableInTouchMode = false
                if (mapViewChild is ViewGroup) {
                    mapViewChild.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mapViewChild.defaultFocusHighlightEnabled = false
                }
            }
            view.foreground = ColorDrawable(android.graphics.Color.TRANSPARENT)
        }
    }
    return foundMapView
}

private fun View.forEachViewInTree(action: (View) -> Unit) {
    action(this)
    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).forEachViewInTree(action)
        }
    }
}

private fun searchSurfaceColor(isDarkMode: Boolean): Color =
    if (isDarkMode) Color(0xFF131618) else Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadarStatusPopup(
    site: NexradSite?,
    metadata: RadarTileMetadata?,
    selectedProduct: RadarProduct,
    onProductSelected: (RadarProduct) -> Unit,
    playbackFrameCount: Int,
    playbackChangesAgo: Int?,
    isPlaybackRunning: Boolean,
    onPlaybackFrameCountSelected: (Int) -> Unit,
    onPlaybackToggled: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    val frameText = playbackChangesAgo?.let { changesAgo ->
        "$changesAgo ago"
    }
    val statusText = when {
        errorMessage != null -> "Unavailable"
        metadata?.tilesReady == false -> "Preparing"
        isLoading -> "Loading"
        metadata?.scanTimeUtc != null -> listOfNotNull(
            metadata.scanTimeUtc.toRadarScanTimeLabel(),
            frameText,
        ).joinToString(" - ")
        frameText != null -> frameText
        else -> selectedProduct.displayName
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = site?.id ?: "Radar",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (errorMessage == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }

                Text(
                    text = selectedProduct.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                RadarProduct.entries.forEachIndexed { index, product ->
                    SegmentedButton(
                        selected = product == selectedProduct,
                        onClick = { onProductSelected(product) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = RadarProduct.entries.size,
                        ),
                        label = { Text(product.displayName) },
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilledIconButton(
                    onClick = onPlaybackToggled,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaybackRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaybackRunning) {
                            "Pause radar playback"
                        } else {
                            "Start radar playback"
                        },
                    )
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    RADAR_PLAYBACK_FRAME_COUNTS.forEachIndexed { index, frameCount ->
                        SegmentedButton(
                            selected = frameCount == playbackFrameCount,
                            onClick = { onPlaybackFrameCountSelected(frameCount) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = RADAR_PLAYBACK_FRAME_COUNTS.size,
                            ),
                            label = { Text(frameCount.toString()) },
                        )
                    }
                }
            }
        }
    }
}

private enum class RadarProduct(
    val pathSegment: String,
    val displayName: String,
) {
    REFLECTIVITY("reflectivity", "Reflectivity"),
    VELOCITY("velocity", "Velocity"),
}

private data class RadarTileMetadataKey(
    val site: String,
    val product: RadarProduct,
    val changesAgo: Int? = null,
)

private data class RadarTileMetadata(
    val site: String,
    val product: RadarProduct,
    val changesAgo: Int?,
    val scanTimeUtc: String,
    val tileSize: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val rasterNativeMaxZoom: Int,
    val vectorTileLayer: String,
    val vectorTileUrl: String,
    val rasterTileUrl: String,
    val tilesReady: Boolean = true,
    val warmupStarted: Boolean = false,
)

private val RadarTileMetadata.key: RadarTileMetadataKey
    get() = RadarTileMetadataKey(site, product, changesAgo)

private val RadarTileMetadata.layerFrameId: String
    get() = changesAgo?.let { "changes-$it" } ?: "latest"

private val RadarTileMetadata.rasterTileRequestUrl: String
    get() {
        val normalizedBaseUrl = BuildConfig.RADAR_TILE_BASE_URL.trimEnd('/')
        return if (rasterTileUrl.startsWith("http://") || rasterTileUrl.startsWith("https://")) {
            rasterTileUrl
        } else {
            "$normalizedBaseUrl/${rasterTileUrl.trimStart('/')}"
        }
    }

private suspend fun fetchRadarTileMetadata(
    siteId: String,
    product: RadarProduct,
    changesAgo: Int? = null,
): RadarTileMetadata =
    withContext(Dispatchers.IO) {
        val normalizedBaseUrl = BuildConfig.RADAR_TILE_BASE_URL.trimEnd('/')
        val framePath = changesAgo?.let { "changes/$it" } ?: "latest"
        val metadataUrl = "$normalizedBaseUrl/radar/$siteId/${product.pathSegment}/$framePath/metadata"
        Log.d(RADAR_LOG_TAG, "GET $metadataUrl")
        val connection = (URL(metadataUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = RADAR_TILE_CONNECT_TIMEOUT_MS
            readTimeout = RADAR_TILE_READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "StormPilot Android")
        }
        val responseBody = connection.readRadarResponseBody()
        Log.d(RADAR_LOG_TAG, "Radar metadata response body=${responseBody.truncateForLog()}")
        val responseJson = JSONObject(responseBody)
        val normalizedSite = responseJson.optString("site", siteId)
            .ifBlank { siteId }
            .uppercase()

        RadarTileMetadata(
            site = normalizedSite,
            product = product,
            changesAgo = changesAgo,
            scanTimeUtc = responseJson.optString("scanTimeUtc"),
            tileSize = responseJson.optInt("tileSize", 512),
            minZoom = responseJson.optInt("minZoom", 5),
            maxZoom = responseJson.optInt("maxZoom", 15),
            rasterNativeMaxZoom = responseJson.optInt(
                "rasterNativeMaxZoom",
                responseJson.optInt("nativeMaxZoom", RADAR_RASTER_NATIVE_MAX_ZOOM),
            ),
            vectorTileLayer = responseJson.optString("vectorTileLayer", product.pathSegment),
            vectorTileUrl = responseJson.optString(
                "vectorTileUrl",
                "/radar/$normalizedSite/${product.pathSegment}/$framePath/{z}/{x}/{y}.mvt",
            ),
            rasterTileUrl = responseJson.optString(
                "rasterTileUrl",
                "/radar/$normalizedSite/${product.pathSegment}/$framePath/{z}/{x}/{y}.png",
            ),
            tilesReady = responseJson.optBoolean("tilesReady", true),
            warmupStarted = responseJson.optBoolean(
                "warmupStarted",
                responseJson.optBoolean("warming", false),
            ),
        )
    }

private fun HttpURLConnection.readRadarResponseBody(): String {
    try {
        val responseCode = responseCode
        val stream = if (responseCode in 200..299) inputStream else errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        Log.d(
            RADAR_LOG_TAG,
            "Radar HTTP $responseCode ${requestMethod.orEmpty()} $url contentType=${contentType.orEmpty()}",
        )
        if (responseCode !in 200..299) {
            throw IOException(
                "Radar metadata request failed with HTTP $responseCode from $url: ${body.truncateForLog()}",
            )
        }
        return body
    } finally {
        disconnect()
    }
}

private fun String.truncateForLog(maxLength: Int = 600): String =
    if (length <= maxLength) this else take(maxLength) + "...(truncated)"

private fun nexradSitesGeoJson(sites: List<NexradSite>): String {
    val features = sites.joinToString(",") { site ->
        """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [${site.longitude}, ${site.latitude}]
          },
          "properties": {
            "id": "${site.id}"
          }
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun String.toRadarScanTimeLabel(): String {
    return try {
        val instant = Instant.parse(this)
        val zoneId = ZoneId.of("America/New_York")
        val formatter = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
            .withZone(zoneId)
        val timePart = formatter.format(instant)
        val isDst = zoneId.rules.isDaylightSavings(instant)
        "$timePart ${if (isDst) "EDT" else "EST"}"
    } catch (e: Exception) {
        val time = substringAfter('T', missingDelimiterValue = "")
            .substringBefore('Z')
            .take(5)
        if (time.length == 5) "$time UTC" else this
    }
}

private const val RADAR_TILE_CONNECT_TIMEOUT_MS = 60_000
private const val RADAR_TILE_READ_TIMEOUT_MS = 60_000
private const val RADAR_TILE_WARMUP_POLL_INTERVAL_MS = 2_000L
private const val RADAR_PLAYBACK_FRAME_DELAY_MS = 700L
private val RADAR_PLAYBACK_FRAME_COUNTS = listOf(6, 12, 18, 24, 30)
private const val RADAR_LOG_TAG = "StormPilotRadar"
private const val RADAR_RASTER_NATIVE_MAX_ZOOM = 10
private val RADAR_TILE_FADE_DURATION = 1.milliseconds
private const val RADAR_SITE_SELECTION_ZOOM = 10.0

private data class NexradSite(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

private val NexradSite.position: Position
    get() = Position(longitude = longitude, latitude = latitude)

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
