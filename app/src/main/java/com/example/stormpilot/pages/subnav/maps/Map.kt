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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import org.maplibre.compose.camera.CameraPosition
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
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsPage(viewModel: MapsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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
        if (origin != null && destination != null && uiState.routeGeoJson != null) {
            val center = Position(
                longitude = (origin.longitude + destination.longitude) / 2.0,
                latitude = (origin.latitude + destination.latitude) / 2.0,
            )
            val latDelta = abs(origin.latitude - destination.latitude)
            val lonDelta = abs(origin.longitude - destination.longitude)
            val span = max(latDelta, lonDelta).coerceAtLeast(0.0005)
            val zoom = (11.5 - log(span, 2.0)).coerceIn(3.0, 16.5)
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(target = center, zoom = zoom),
                duration = 1.seconds,
            )
        }
    }

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    StormPilotTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                baseStyle = BaseStyle.Uri("https://api.protomaps.com/styles/v5/dark/en.json?key=64a5f0a9c35b4ca1"),
                cameraState = cameraState,
                onMapLongClick = { point ->
                    viewModel.onDestinationSelected(point)
                    true
                },
                modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        padding = PaddingValues(0.dp),
                        isLogoEnabled = false,
                        logoAlignment = Alignment.BottomStart,
                        isAttributionEnabled = false,
                        attributionAlignment = Alignment.BottomEnd,
                        isCompassEnabled = true,
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

                val routeSource = rememberGeoJsonSource(
                    data = uiState.routeGeoJson ?: GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")
                )
                LineLayer(
                    id = "route-line",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.tertiary),
                    width = const(7.dp),
                    opacity = const(0.9),
                )
            }

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

            TripSummaryCard(
                state = uiState,
                onRetry = viewModel::retryRoute,
                onClearRoute = viewModel::clearRoute,
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScaffold(
    modifier: Modifier = Modifier,
    query: String,
    active: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onResultClick: (String) -> Unit,
) {
    SearchBar(
        modifier = modifier,
        query = query,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.inverseOnSurface),
        onQueryChange = onQueryChange,
        onSearch = { onActiveChange(false) },
        windowInsets = WindowInsets(0, 0, 0, 0),
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Search here") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (active) {
                IconButton(onClick = { if (query.isNotEmpty()) onQueryChange("") else onActiveChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        shape = SearchBarDefaults.inputFieldShape,
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            AnimatedVisibility(
                visible = active,
                enter = fadeIn(animationSpec = tween(0)) + expandVertically(),
                exit = fadeOut(),
            ) {
                val recents = List(3) { index -> "Recent Location $index" }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(recents) { result ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "123 Street Name, City",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onResultClick(result) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSummaryCard(
    state: MapsUiState,
    onRetry: () -> Unit,
    onClearRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.destination == null && state.routeError == null && !state.isLoadingRoute) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isLoadingRoute) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(end = 4.dp))
                    Text("Loading route…")
                }
            }

            state.routeError?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Retry", modifier = Modifier.clickable { onRetry() }, color = MaterialTheme.colorScheme.primary)
                    Text("Clear", modifier = Modifier.clickable { onClearRoute() }, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (state.distanceMeters != null && state.durationSeconds != null) {
                Text(
                    text = "${formatDistance(state.distanceMeters)} • ${formatDuration(state.durationSeconds)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                state.steps.take(4).forEachIndexed { index, step ->
                    Text(text = "${index + 1}. ${step.instruction}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Clear route", modifier = Modifier.clickable { onClearRoute() }, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
