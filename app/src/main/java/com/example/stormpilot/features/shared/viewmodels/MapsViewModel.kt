package com.example.stormpilot.features.shared.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.shared.data.alerts.AlertsRepository
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import com.example.stormpilot.features.shared.data.alerts.bestMatchForEvent
import com.example.stormpilot.features.shared.data.location.LocationLookupRepository
import com.example.stormpilot.features.shared.data.routing.RouteStep
import com.example.stormpilot.features.shared.data.routing.RoutingParsing
import com.example.stormpilot.features.shared.data.routing.RoutingRepository
import com.example.stormpilot.features.shared.data.search.PhotonApiClient
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.Position
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
    val userBearingDegrees: Double? = null,
    val navigationBearingDegrees: Double? = null,
    val isOffRoute: Boolean = false,
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
    private val locationLookupRepository: LocationLookupRepository,
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
    private var routeRequestJob: Job? = null
    private var rerouteDebounceJob: Job? = null
    private var routeWarningsJob: Job? = null
    private var alertDetailJob: Job? = null
    private var latestAlertsGeoJson: String? = null
    private var stormAvoidanceForCurrentTrip = false

    fun onUserLocationUpdated(position: Position, bearingDegrees: Double? = null) {
        val currentState = _uiState.value
        val normalizedUserBearing = bearingDegrees?.normalizeBearingDegrees()
        val hasActiveRoute = currentState.destination != null && currentRoutePolyline.isNotEmpty()
        val minRouteDistance = if (hasActiveRoute) {
            minDistanceMetersToPolyline(position, currentRoutePolyline)
        } else {
            null
        }
        val isOffRoute = minRouteDistance?.let { it > OFF_ROUTE_THRESHOLD_METERS } ?: false
        val navigationBearing = if (hasActiveRoute) {
            navigationCameraBearingDegrees(
                userPosition = position,
                routePolyline = currentRoutePolyline,
                isOffRoute = isOffRoute,
                userBearingDegrees = normalizedUserBearing ?: currentState.userBearingDegrees,
                previousBearingDegrees = currentState.navigationBearingDegrees,
            )
        } else {
            normalizedUserBearing ?: currentState.userBearingDegrees ?: currentState.navigationBearingDegrees
        }
        val updatedState = currentState.copy(
            origin = position,
            userBearingDegrees = normalizedUserBearing ?: currentState.userBearingDegrees,
            navigationBearingDegrees = navigationBearing,
            isOffRoute = isOffRoute,
        )
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
            if (isOffRoute) {
                scheduleReroute()
            }
        }
    }

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
            navigationBearingDegrees = _uiState.value.userBearingDegrees,
            isOffRoute = false,
            routeError = null,
            isLoadingRoute = false,
            address = featureAddress,
            routeWarningCount = null,
            routeWarningError = null,
            stormRouteAlertMessage = null,
        )
        currentRoutePolyline = emptyList()
        stormAvoidanceForCurrentTrip = false
        rerouteDebounceJob?.cancel()
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
        val results = photonApiClient.search(trimmedQuery, origin)
            .withStraightLineDistances(origin)
            .sortedByClosest()
        currentCoroutineContext().ensureActive()
        _searchResults.value = results

        _isSearching.value = false
    }

    private suspend fun fetchAlerts() {
        try {
            val geojson = alertsRepository.fetchWarningPolygonsGeoJson()
            latestAlertsGeoJson = geojson
            _uiState.update { it.copy(alertsGeoJson = GeoJsonData.JsonString(geojson)) }
            rerouteIfStormAwareRouteHasWarnings(geojson)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Alerts", "Failed to fetch alerts: ${e.message}")
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
            navigationBearingDegrees = _uiState.value.userBearingDegrees,
            isOffRoute = false,
            routeError = null,
            isLoadingRoute = false,
            address = "Locating...",
            routeWarningCount = null,
            routeWarningError = null,
            stormRouteAlertMessage = null,
        )
        currentRoutePolyline = emptyList()
        stormAvoidanceForCurrentTrip = false
        rerouteDebounceJob?.cancel()
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
        routeRequestJob?.cancel()
        rerouteDebounceJob?.cancel()
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
            navigationBearingDegrees = null,
            isOffRoute = false,
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

        routeRequestJob?.cancel()
        routeRequestJob = viewModelScope.launch {
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
            routingRepository.fetchRouteSelection(
                origin = origin,
                destination = destination,
                avoidStorms = shouldAvoidStorms(),
                includeWarnings = true,
            )
                .onSuccess { selection ->
                    val route = selection.route
                    currentRoutePolyline = route.polyline
                    val latestState = _uiState.value
                    val latestOrigin = latestState.origin ?: origin
                    val navigationBearing = navigationCameraBearingDegrees(
                        userPosition = latestOrigin,
                        routePolyline = route.polyline,
                        isOffRoute = false,
                        userBearingDegrees = latestState.userBearingDegrees,
                        previousBearingDegrees = latestState.navigationBearingDegrees,
                    )
                    val geoJson = GeoJsonData.JsonString(
                        selection.routeGeoJson.ifBlank {
                            RoutingParsing.toGeoJsonLineString(route.polyline)
                        },
                    )
                    _uiState.value = latestState.copy(
                        routeGeoJson = geoJson,
                        routeStart = route.polyline.firstOrNull(),
                        routeEnd = route.polyline.lastOrNull(),
                        distanceMeters = route.distanceMeters,
                        durationSeconds = route.durationSeconds,
                        remainingDistanceMeters = route.distanceMeters,
                        remainingDurationSeconds = route.durationSeconds,
                        steps = route.steps,
                        currentStepIndex = 0,
                        navigationBearingDegrees = navigationBearing,
                        isOffRoute = false,
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

    private suspend fun rerouteIfStormAwareRouteHasWarnings(alertsGeoJson: String) {
        if (!shouldAvoidStorms() || currentRoutePolyline.size < 2) return

        val destination = _uiState.value.destination
        val analysis = routingRepository.analyzeRouteWarnings(
            routePolyline = currentRoutePolyline,
            destination = destination,
            alertsGeoJson = alertsGeoJson,
        ).getOrElse { error ->
            Log.e("RouteWarnings", "Failed to analyze route warnings: ${error.message}")
            _uiState.update {
                it.copy(
                    routeWarningCount = null,
                    routeWarningError = "Warnings unavailable",
                )
            }
            return
        }
        currentCoroutineContext().ensureActive()

        val retainedWarningMessage = _uiState.value.routeWarningError?.takeIf {
            it == NO_STORM_FREE_ROUTE_MESSAGE ||
                    it == DESTINATION_IN_WARNING_MESSAGE
        }
        val warningMessage = when {
            analysis.warningCount == 0 -> null
            analysis.destinationInsideWarning -> DESTINATION_IN_WARNING_MESSAGE
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
                alertsRepository.fetchWarningPolygonsGeoJson().also { geojson ->
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
            routingRepository.analyzeRouteWarnings(
                routePolyline = routePolyline,
                destination = destination,
                alertsGeoJson = alertsGeoJson,
            ).onSuccess { analysis ->
                currentCoroutineContext().ensureActive()

                _uiState.update {
                    it.copy(
                        routeWarningCount = analysis.warningCount,
                        routeWarningError = null,
                    )
                }
            }.onFailure { error ->
                Log.e("RouteWarnings", "Failed to analyze route warnings: ${error.message}")
                _uiState.update {
                    it.copy(
                        routeWarningCount = null,
                        routeWarningError = "Warnings unavailable",
                    )
                }
            }
        }
    }

    companion object {
        private const val OFF_ROUTE_THRESHOLD_METERS = 40.0
        private const val STEP_REACHED_THRESHOLD_METERS = 25.0
        private const val REROUTE_DEBOUNCE_MS = 1_500L
        private const val DESTINATION_IN_WARNING_MESSAGE =
            "Destination is inside a storm warning polygon. This route enters the warning area to reach it."
        private const val NO_STORM_FREE_ROUTE_MESSAGE =
            "No storm-free road route was found. This route passes through a storm warning polygon."
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
        return locationLookupRepository.reverseLocation(position.latitude, position.longitude)
            .getOrNull()
            ?.name
            ?.takeIf { it.isNotBlank() }
            ?: String.format(Locale.US, "Point: %.4f, %.4f", position.latitude, position.longitude)
    }
}

private fun minDistanceMetersToPolyline(point: Position, polyline: List<Position>): Double {
    if (polyline.size < 2) return Double.MAX_VALUE
    return polyline.windowed(2).minOf { segment ->
        distancePointToSegmentMeters(point, segment[0], segment[1])
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
