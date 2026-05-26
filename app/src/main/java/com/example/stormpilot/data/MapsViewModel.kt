package com.example.stormpilot.data

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.Position
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.*

data class MapsUiState(
    val origin: Position? = null,
    val destination: Position? = null,
    val routeGeoJson: GeoJsonData? = null,
    val routeStart: Position? = null,
    val routeEnd: Position? = null,
    val distanceMeters: Double? = null,
    val durationSeconds: Double? = null,
    val remainingDistanceMeters: Double? = null,
    val remainingDurationSeconds: Double? = null,
    val steps: List<RouteStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isLoadingRoute: Boolean = false,
    val routeError: String? = null,
    val address: String? = null,
    val alertsGeoJson: GeoJsonData? = null,
    val routeWarningCount: Int? = null,
    val routeWarningError: String? = null,
)

/**
 * Owns map search, routing, alert overlays, and navigation progress state for the map screen.
 */
@HiltViewModel
@OptIn(FlowPreview::class)
class MapsViewModel @Inject constructor(
    private val routingRepository: RoutingRepository,
    private val photonApiClient: PhotonApiClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PhotonFeature>>(emptyList())
    val searchResults: StateFlow<List<PhotonFeature>> = _searchResults.asStateFlow()

    private val _selectedLocation = MutableStateFlow<PhotonFeature?>(null)
    val selectedLocation: StateFlow<PhotonFeature?> = _selectedLocation.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var currentRoutePolyline: List<Position> = emptyList()
    private var rerouteDebounceJob: Job? = null
    private var routeWarningsJob: Job? = null
    private var latestAlertsGeoJson: String? = null

    fun onUserLocationUpdated(position: Position) {
        val updatedState = _uiState.value.copy(origin = position)
        _uiState.value = updatedState

        if (
            updatedState.destination != null &&
            currentRoutePolyline.isEmpty() &&
            !updatedState.isLoadingRoute &&
            updatedState.routeError == null
        ) {
            requestRoute()
            return
        }

        if (updatedState.destination != null && currentRoutePolyline.isNotEmpty()) {
            advanceStepProgress(position)
            val minDistance = minDistanceMetersToPolyline(position, currentRoutePolyline)
            if (minDistance > OFF_ROUTE_THRESHOLD_METERS) {
                scheduleReroute()
            }
        }
    }

    private val alertTypes = listOf("Tornado Warning", "Severe Thunderstorm Warning", "Flash Flood Warning")

    init {
        viewModelScope.launch {
            while (true) {
                fetchAlerts()
                delay(300_000L)
            }
        }

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collectLatest { query ->
                    if (query.length > 2) {
                        _isSearching.value = true
                        _searchResults.value = photonApiClient.search(query, _uiState.value.origin)
                        _isSearching.value = false
                    } else {
                        _searchResults.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
        } else if (query.length > 2) {
            _isSearching.value = true
        }
    }

    fun onLocationSelected(feature: PhotonFeature) {
        _selectedLocation.value = feature
        _searchQuery.value = feature.name
        _searchResults.value = emptyList()

        val featureAddress = listOfNotNull(feature.name, feature.city, feature.state).joinToString(", ")

        _uiState.value = _uiState.value.copy(
            destination = feature.geometry,
            routeGeoJson = null,
            distanceMeters = null,
            durationSeconds = null,
            remainingDistanceMeters = null,
            remainingDurationSeconds = null,
            steps = emptyList(),
            currentStepIndex = 0,
            routeError = null,
            isLoadingRoute = false,
            address = featureAddress,
            routeWarningCount = null,
            routeWarningError = null,
        )
        currentRoutePolyline = emptyList()
        routeWarningsJob?.cancel()
        requestRoute()
    }

    private suspend fun fetchAlerts() {
        try {
            val geojson = fetchAlertsGeoJson()
            latestAlertsGeoJson = geojson
            _uiState.update { it.copy(alertsGeoJson = GeoJsonData.JsonString(geojson)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Alerts", "Failed to fetch alerts: ${e.message}")
        }
    }

    private suspend fun fetchAlertsGeoJson(): String = withContext(Dispatchers.IO) {
        val where = alertTypes.joinToString(",") { "'$it'" }.let { "prod_type IN ($it)" }
        val encoded = URLEncoder.encode(where, "UTF-8")
        val url = URL(
            "https://mapservices.weather.noaa.gov/eventdriven/rest/services/WWA/watch_warn_adv/MapServer/1/query" +
                    "?where=$encoded&outFields=prod_type&geometryType=esriGeometryPolygon" +
                    "&spatialRel=esriSpatialRelIntersects&outSR=4326&f=geojson"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/geo+json")
        }

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(responseCode in 200..299) { "Alerts request failed with HTTP $responseCode: $body" }
            body
        } finally {
            connection.disconnect()
        }
    }

    fun onDestinationSelected(position: Position) {
        _uiState.value = _uiState.value.copy(
            destination = position,
            routeGeoJson = null,
            distanceMeters = null,
            durationSeconds = null,
            remainingDistanceMeters = null,
            remainingDurationSeconds = null,
            steps = emptyList(),
            currentStepIndex = 0,
            routeError = null,
            isLoadingRoute = false,
            address = "Locating...",
            routeWarningCount = null,
            routeWarningError = null,
        )
        currentRoutePolyline = emptyList()
        routeWarningsJob?.cancel()

        viewModelScope.launch {
            val result = fetchAddress(position)
            _uiState.update { it.copy(address = result) }
        }

        requestRoute()
    }

    fun retryRoute() {
        requestRoute()
    }

    fun requestDirections() {
        requestRoute()
    }

    fun clearRoute() {
        currentRoutePolyline = emptyList()
        routeWarningsJob?.cancel()
        _uiState.value = _uiState.value.copy(
            destination = null,
            routeGeoJson = null,
            routeStart = null,
            routeEnd = null,
            distanceMeters = null,
            durationSeconds = null,
            remainingDistanceMeters = null,
            remainingDurationSeconds = null,
            steps = emptyList(),
            currentStepIndex = 0,
            routeError = null,
            isLoadingRoute = false,
            address = null,
            routeWarningCount = null,
            routeWarningError = null,
        )
    }

    private fun scheduleReroute() {
        if (rerouteDebounceJob?.isActive == true) return
        rerouteDebounceJob = viewModelScope.launch {
            delay(REROUTE_DEBOUNCE_MS)
            requestRoute()
        }
    }

    private fun requestRoute() {
        val state = _uiState.value
        val origin = state.origin ?: return
        val destination = state.destination ?: return

        viewModelScope.launch {
            Log.d("Routing", "Starting route request")
            val start = System.currentTimeMillis()
            routeWarningsJob?.cancel()
            _uiState.value = _uiState.value.copy(
                isLoadingRoute = true,
                routeError = null,
                routeWarningCount = null,
                routeWarningError = null,
            )
            routingRepository.fetchRoute(origin, destination)
                .onSuccess { route ->
                    currentRoutePolyline = route.polyline
                    val geoJson = GeoJsonData.JsonString(
                        RoutingParsing.toGeoJsonLineString(route.polyline)
                    )
                    _uiState.value = _uiState.value.copy(
                        routeGeoJson = geoJson,
                        routeStart = route.polyline.firstOrNull(),
                        routeEnd = route.polyline.lastOrNull(),
                        distanceMeters = route.distanceMeters,
                        durationSeconds = route.durationSeconds,
                        remainingDistanceMeters = route.distanceMeters,
                        remainingDurationSeconds = route.durationSeconds,
                        steps = route.steps,
                        currentStepIndex = 0,
                        isLoadingRoute = false,
                        routeError = null,
                        routeWarningCount = null,
                        routeWarningError = null,
                    )
                    checkRouteWarnings(route.polyline)
                }
                .onFailure { error ->
                    routeWarningsJob?.cancel()
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoute = false,
                        routeError = error.message ?: "Failed to fetch route",
                        routeWarningCount = null,
                        routeWarningError = null,
                    )
                }
            Log.d("Routing", "Route finished in ${System.currentTimeMillis() - start}ms")

        }
    }

    private fun checkRouteWarnings(routePolyline: List<Position>) {
        routeWarningsJob?.cancel()
        routeWarningsJob = viewModelScope.launch {
            if (routePolyline.size < 2) {
                _uiState.update { it.copy(routeWarningCount = 0, routeWarningError = null) }
                return@launch
            }

            _uiState.update { it.copy(routeWarningCount = null, routeWarningError = null) }

            val alertsGeoJson = try {
                fetchAlertsGeoJson().also { geojson ->
                    latestAlertsGeoJson = geojson
                    _uiState.update { it.copy(alertsGeoJson = GeoJsonData.JsonString(geojson)) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RouteWarnings", "Failed to fetch route warnings: ${e.message}")
                latestAlertsGeoJson
            }

            if (alertsGeoJson == null) {
                _uiState.update {
                    it.copy(
                        routeWarningCount = null,
                        routeWarningError = "Warnings unavailable",
                    )
                }
                return@launch
            }

            val warningCount = withContext(Dispatchers.Default) {
                RouteWarningCounter.countWarningsIntersectingRoute(alertsGeoJson, routePolyline)
            }
            currentCoroutineContext().ensureActive()

            _uiState.update {
                it.copy(
                    routeWarningCount = warningCount,
                    routeWarningError = null,
                )
            }
        }
    }

    companion object {
        private const val OFF_ROUTE_THRESHOLD_METERS = 40.0
        private const val STEP_REACHED_THRESHOLD_METERS = 25.0
        private const val REROUTE_DEBOUNCE_MS = 8_000L
    }

    private fun advanceStepProgress(userPosition: Position) {
        val currentState = _uiState.value
        val steps = currentState.steps
        if (steps.isEmpty()) return

        var nextStepIndex = currentState.currentStepIndex.coerceAtMost(steps.lastIndex)
        val maneuverPosition = steps[nextStepIndex].maneuverLocation
        if (maneuverPosition != null) {
            val distanceToManeuver = haversineMeters(userPosition, maneuverPosition)
            if (distanceToManeuver <= STEP_REACHED_THRESHOLD_METERS && nextStepIndex < steps.lastIndex) {
                nextStepIndex += 1
            }
        }

        val completedDistance = steps.take(nextStepIndex).sumOf { it.distanceMeters }
        val completedDuration = steps.take(nextStepIndex).sumOf { it.durationSeconds }
        val totalDistance = currentState.distanceMeters ?: 0.0
        val totalDuration = currentState.durationSeconds ?: 0.0

        _uiState.value = currentState.copy(
            currentStepIndex = nextStepIndex,
            remainingDistanceMeters = (totalDistance - completedDistance).coerceAtLeast(0.0),
            remainingDurationSeconds = (totalDuration - completedDuration).coerceAtLeast(0.0),
        )
    }

    private suspend fun fetchAddress(position: Position): String {
        return withContext(Dispatchers.IO) {
            try {
                // Use the 'context' injected in the constructor
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val name = addr.featureName ?: ""
                    val street = addr.thoroughfare ?: ""

                    if (name == street || street.isEmpty()) name else "$name $street"
                } else {
                    "Unknown Location"
                }
            } catch (e: Exception) {
                "Point: ${"%.4f".format(position.latitude)}, ${"%.4f".format(position.longitude)}"
            }
        }
    }
}

private fun minDistanceMetersToPolyline(point: Position, polyline: List<Position>): Double {
    if (polyline.size < 2) return Double.MAX_VALUE
    return polyline.windowed(2).minOf { segment ->
        distancePointToSegmentMeters(point, segment[0], segment[1])
    }
}

private fun distancePointToSegmentMeters(point: Position, start: Position, end: Position): Double {
    val lat0 = Math.toRadians(point.latitude)

    val px = (Math.toRadians(point.longitude - start.longitude)) * cos(lat0)
    val py = Math.toRadians(point.latitude - start.latitude)
    val sx = 0.0
    val sy = 0.0
    val ex = (Math.toRadians(end.longitude - start.longitude)) * cos(lat0)
    val ey = Math.toRadians(end.latitude - start.latitude)

    val dx = ex - sx
    val dy = ey - sy
    val lengthSq = dx * dx + dy * dy

    val t = if (lengthSq == 0.0) 0.0 else ((px - sx) * dx + (py - sy) * dy) / lengthSq
    val clampedT = t.coerceIn(0.0, 1.0)

    val closestX = sx + clampedT * dx
    val closestY = sy + clampedT * dy

    val deltaX = px - closestX
    val deltaY = py - closestY

    return sqrt(deltaX * deltaX + deltaY * deltaY) * EARTH_RADIUS_METERS
}

private fun haversineMeters(a: Position, b: Position): Double {
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)

    val sinLat = sin(dLat / 2.0)
    val sinLon = sin(dLon / 2.0)
    val h = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
    return 2.0 * EARTH_RADIUS_METERS * asin(sqrt(h))
}

private const val EARTH_RADIUS_METERS = 6_371_000.0
