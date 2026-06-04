package com.example.stormpilot.features.map.presentation

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.alerts.data.AlertsRepository
import com.example.stormpilot.features.alerts.data.NwsAlert
import com.example.stormpilot.features.alerts.data.bestMatchForEvent
import com.example.stormpilot.features.map.data.routing.RouteResult
import com.example.stormpilot.features.map.data.routing.RouteStep
import com.example.stormpilot.features.map.data.routing.RouteWarningCounter
import com.example.stormpilot.features.map.data.routing.RoutingParsing
import com.example.stormpilot.features.map.data.routing.RoutingRepository
import com.example.stormpilot.features.map.data.routing.StormDetourPlanner
import com.example.stormpilot.features.map.data.routing.StormRoutePolicy
import com.example.stormpilot.features.map.data.search.PhotonApiClient
import com.example.stormpilot.features.map.data.search.PhotonFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    val isAlertDetailVisible: Boolean = false,
    val selectedAlert: NwsAlert? = null,
    val isAlertDetailLoading: Boolean = false,
    val alertDetailError: String? = null,
    val stormRouteAlertMessage: String? = null,
)

/**
 * Owns map search, routing, alert overlays, and navigation progress state for the map screen.
 */
@HiltViewModel
@OptIn(FlowPreview::class)
class MapsViewModel @Inject constructor(
    private val routingRepository: RoutingRepository,
    private val photonApiClient: PhotonApiClient,
    private val alertsRepository: AlertsRepository,
    @param:ApplicationContext private val context: Context,
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
    private var alertDetailJob: Job? = null
    private var latestAlertsGeoJson: String? = null
    private var stormAvoidanceForCurrentTrip = false

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
                    runSearch(query)
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

    fun onSearchSubmitted() {
        val query = _searchQuery.value.trim()
        if (query.length <= 2) return

        viewModelScope.launch {
            if (_searchResults.value.isEmpty() || _isSearching.value) {
                runSearch(query)
            }

            val selectedResult = _searchResults.value.getOrNull(9)
                ?: _searchResults.value.lastOrNull()
                ?: return@launch
            onLocationSelected(selectedResult)
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
            stormRouteAlertMessage = null,
        )
        currentRoutePolyline = emptyList()
        stormAvoidanceForCurrentTrip = false
        routeWarningsJob?.cancel()
        requestRoute()
    }

    private suspend fun runSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length <= 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        _isSearching.value = true
        val origin = _uiState.value.origin
        val baseResults = photonApiClient.search(trimmedQuery, origin)
            .withStraightLineDistances(origin)
            .sortedByClosest()
        currentCoroutineContext().ensureActive()
        _searchResults.value = baseResults

        if (origin != null && baseResults.isNotEmpty()) {
            val enrichedResults = photonApiClient.enrichWithDrivingMetrics(origin, baseResults)
                .sortedByClosest()
            currentCoroutineContext().ensureActive()
            _searchResults.value = enrichedResults
        }

        _isSearching.value = false
    }

    private suspend fun fetchAlerts() {
        try {
            val geojson = fetchAlertsGeoJson()
            latestAlertsGeoJson = geojson
            _uiState.update { it.copy(alertsGeoJson = GeoJsonData.JsonString(geojson)) }
            rerouteIfStormAwareRouteHasWarnings(geojson)
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
            stormRouteAlertMessage = null,
        )
        currentRoutePolyline = emptyList()
        stormAvoidanceForCurrentTrip = false
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

    fun showAlertDetail(alert: NwsAlert) {
        alertDetailJob?.cancel()
        _uiState.update {
            it.copy(
                isAlertDetailVisible = true,
                selectedAlert = alert,
                isAlertDetailLoading = false,
                alertDetailError = null,
            )
        }
    }

    fun dismissAlertDetail() {
        alertDetailJob?.cancel()
        _uiState.update {
            it.copy(
                isAlertDetailVisible = false,
                selectedAlert = null,
                isAlertDetailLoading = false,
                alertDetailError = null,
            )
        }
    }

    fun dismissStormRouteAlert() {
        _uiState.update { it.copy(stormRouteAlertMessage = null) }
    }

    fun onAlertPolygonTapped(position: Position, eventType: String) {
        alertDetailJob?.cancel()
        alertDetailJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAlertDetailVisible = true,
                    selectedAlert = null,
                    isAlertDetailLoading = true,
                    alertDetailError = null,
                )
            }

            alertsRepository.fetchAlerts(position.latitude, position.longitude)
                .onSuccess { alerts ->
                    val matchingAlert = alerts.bestMatchForEvent(eventType)
                    _uiState.update {
                        it.copy(
                            isAlertDetailVisible = true,
                            selectedAlert = matchingAlert,
                            isAlertDetailLoading = false,
                            alertDetailError = if (matchingAlert == null) {
                                "No active NWS detail text was found for this alert polygon."
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAlertDetailVisible = true,
                            selectedAlert = null,
                            isAlertDetailLoading = false,
                            alertDetailError = error.message ?: "Unable to load alert details.",
                        )
                    }
                }
        }
    }

    fun requestDirections() {
        requestRoute()
    }

    fun requestSafeDirections() {
        stormAvoidanceForCurrentTrip = true
        requestRoute()
    }

    fun clearRoute() {
        currentRoutePolyline = emptyList()
        stormAvoidanceForCurrentTrip = false
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
            stormRouteAlertMessage = null,
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
                stormRouteAlertMessage = null,
            )
            fetchRouteSelection(origin, destination, shouldAvoidStorms())
                .onSuccess { selection ->
                    val route = selection.route
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
                        routeWarningCount = selection.warningCount,
                        routeWarningError = selection.warningError,
                        stormRouteAlertMessage = selection.stormRouteAlertMessage,
                    )
                    if (selection.warningCount == null && selection.warningError == null) {
                        checkRouteWarnings(route.polyline)
                    }
                }
                .onFailure { error ->
                    routeWarningsJob?.cancel()
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoute = false,
                        routeError = error.message ?: "Failed to fetch route",
                        routeWarningCount = null,
                        routeWarningError = null,
                        stormRouteAlertMessage = null,
                    )
                }
            Log.d("Routing", "Route finished in ${System.currentTimeMillis() - start}ms")

        }
    }

    private fun shouldAvoidStorms(): Boolean = AppSettings.avoidStorms || stormAvoidanceForCurrentTrip

    private suspend fun fetchRouteSelection(
        origin: Position,
        destination: Position,
        avoidStorms: Boolean,
    ): Result<RouteSelection> {
        if (!avoidStorms) {
            return routingRepository.fetchRoute(origin, destination)
                .map { route -> RouteSelection(route = route) }
        }

        return runCatching {
            val alertsGeoJson = fetchFreshAlertsForRouting()
            val baseCandidates = routingRepository.fetchRouteCandidates(origin, destination).getOrThrow()
            require(baseCandidates.isNotEmpty()) { "No route returned" }

            if (alertsGeoJson == null) {
                return@runCatching RouteSelection(
                    route = baseCandidates.first(),
                    warningError = "Warnings unavailable",
                )
            }

            val baseSelection = withContext(Dispatchers.Default) {
                StormRoutePolicy.selectRoute(
                    candidates = baseCandidates,
                    alertsGeoJson = alertsGeoJson,
                    destination = destination,
                )
            }
            val shouldProbeDetours = baseSelection.warningCount > 0 &&
                    baseSelection.warningMessage != StormRoutePolicy.DESTINATION_IN_WARNING_MESSAGE
            val candidates = if (shouldProbeDetours) {
                (baseCandidates + fetchStormDetourCandidates(
                    origin = origin,
                    destination = destination,
                    alertsGeoJson = alertsGeoJson,
                    seedRoutes = baseCandidates,
                )).distinctByRouteGeometry()
            } else {
                baseCandidates
            }

            val selection = if (candidates.size == baseCandidates.size) {
                baseSelection
            } else {
                withContext(Dispatchers.Default) {
                    StormRoutePolicy.selectRoute(
                        candidates = candidates,
                        alertsGeoJson = alertsGeoJson,
                        destination = destination,
                    )
                }
            }

            RouteSelection(
                route = selection.route,
                warningCount = selection.warningCount,
                warningError = selection.warningMessage,
                stormRouteAlertMessage = selection.warningMessage,
            )
        }
    }

    private suspend fun fetchStormDetourCandidates(
        origin: Position,
        destination: Position,
        alertsGeoJson: String,
        seedRoutes: List<RouteResult>,
    ): List<RouteResult> {
        val waypointSets = withContext(Dispatchers.Default) {
            StormDetourPlanner.buildWaypointSets(
                alertsGeoJson = alertsGeoJson,
                seedRoutes = seedRoutes,
                origin = origin,
                destination = destination,
            )
        }
        if (waypointSets.isEmpty()) return emptyList()

        val detourCandidates = mutableListOf<RouteResult>()
        for (chunk in waypointSets.chunked(DETOUR_ROUTE_CONCURRENCY)) {
            val chunkCandidates = coroutineScope {
                chunk.map { waypoints ->
                    async {
                        routingRepository.fetchRouteVia(
                            origin = origin,
                            destination = destination,
                            waypoints = waypoints,
                        ).getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }

            detourCandidates += chunkCandidates
            val foundStormFreeRoute = withContext(Dispatchers.Default) {
                chunkCandidates.any { route ->
                    RouteWarningCounter.analyzeWarnings(
                        alertsGeoJson = alertsGeoJson,
                        routePolyline = route.polyline,
                        destination = destination,
                    ).warningCount == 0
                }
            }
            if (foundStormFreeRoute) break
        }

        return detourCandidates
    }

    private suspend fun fetchFreshAlertsForRouting(): String? {
        return try {
            fetchAlertsGeoJson().also { geojson ->
                latestAlertsGeoJson = geojson
                _uiState.update { it.copy(alertsGeoJson = GeoJsonData.JsonString(geojson)) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("StormRouting", "Failed to fetch alerts for storm-aware routing: ${e.message}")
            latestAlertsGeoJson
        }
    }

    private suspend fun rerouteIfStormAwareRouteHasWarnings(alertsGeoJson: String) {
        if (!shouldAvoidStorms() || currentRoutePolyline.size < 2) return

        val destination = _uiState.value.destination
        val analysis = withContext(Dispatchers.Default) {
            RouteWarningCounter.analyzeWarnings(alertsGeoJson, currentRoutePolyline, destination)
        }
        currentCoroutineContext().ensureActive()

        val retainedWarningMessage = _uiState.value.routeWarningError?.takeIf {
            it == StormRoutePolicy.NO_STORM_FREE_ROUTE_MESSAGE ||
                    it == StormRoutePolicy.DESTINATION_IN_WARNING_MESSAGE
        }
        val warningMessage = when {
            analysis.warningCount == 0 -> null
            analysis.destinationInsideWarning -> StormRoutePolicy.DESTINATION_IN_WARNING_MESSAGE
            else -> retainedWarningMessage
        }

        _uiState.update {
            it.copy(
                routeWarningCount = analysis.warningCount,
                routeWarningError = warningMessage,
            )
        }

        if (
            analysis.warningCount > 0 &&
            !analysis.destinationInsideWarning &&
            _uiState.value.destination != null &&
            !_uiState.value.isLoadingRoute
        ) {
            scheduleReroute()
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

            val destination = _uiState.value.destination
            val analysis = withContext(Dispatchers.Default) {
                RouteWarningCounter.analyzeWarnings(alertsGeoJson, routePolyline, destination)
            }
            currentCoroutineContext().ensureActive()

            _uiState.update {
                it.copy(
                    routeWarningCount = analysis.warningCount,
                    routeWarningError = null,
                )
            }
        }
    }

    private data class RouteSelection(
        val route: RouteResult,
        val warningCount: Int? = null,
        val warningError: String? = null,
        val stormRouteAlertMessage: String? = null,
    )

    companion object {
        private const val OFF_ROUTE_THRESHOLD_METERS = 40.0
        private const val STEP_REACHED_THRESHOLD_METERS = 25.0
        private const val REROUTE_DEBOUNCE_MS = 8_000L
        private const val DETOUR_ROUTE_CONCURRENCY = 4
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

private fun List<RouteResult>.distinctByRouteGeometry(): List<RouteResult> =
    distinctBy { route ->
        route.polyline.joinToString(separator = "|") { position ->
            String.format(Locale.US, "%.5f,%.5f", position.longitude, position.latitude)
        }
    }

private fun List<PhotonFeature>.withStraightLineDistances(origin: Position?): List<PhotonFeature> {
    if (origin == null) return this
    return map { feature ->
        feature.copy(straightLineDistanceMeters = haversineMeters(origin, feature.geometry))
    }
}

private fun List<PhotonFeature>.sortedByClosest(): List<PhotonFeature> =
    sortedWith(
        compareBy<PhotonFeature> {
            it.driveDistanceMeters ?: it.straightLineDistanceMeters ?: Double.MAX_VALUE
        }.thenBy { it.name }
    )

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
