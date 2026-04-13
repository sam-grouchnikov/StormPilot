package com.example.stormpilot.pages.subnav.maps.viewmodel

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.pages.subnav.maps.routing.RouteStep
import com.example.stormpilot.pages.subnav.maps.routing.RoutingParsing
import com.example.stormpilot.pages.subnav.maps.routing.RoutingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import kotlin.math.*

/**
 * Represents the UI state for the Maps screen, encompassing routing data,
 */
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
)

/**
 * ViewModel responsible for managing the state and logic of the Maps screen.
 *
 * This ViewModel handles route calculation, user location tracking, off-route detection,
 * and navigation progress updates. It interacts with the [RoutingRepository] to fetch
 * geographic data and exposes a [MapsUiState] for the UI to observe.
 *
 * Key features include:
 * - Requesting and parsing routes between an origin and destination.
 * - Real-time tracking of user progress through navigation steps.
 * - Automatic rerouting logic when the user deviates from the path by a specific threshold.
 * - Conversion of route coordinates into GeoJSON for map rendering.
 *
 * @property routingRepository The repository used to fetch route information from a navigation service.
 */
@HiltViewModel
class MapsViewModel @Inject constructor(
    private val routingRepository: RoutingRepository,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private var currentRoutePolyline: List<Position> = emptyList()
    private var rerouteDebounceJob: Job? = null

    fun onUserLocationUpdated(position: Position) {
        val previousOrigin = _uiState.value.origin
        _uiState.value = _uiState.value.copy(origin = position)

        if (_uiState.value.destination != null && currentRoutePolyline.isNotEmpty()) {
            advanceStepProgress(position)
            val minDistance = minDistanceMetersToPolyline(position, currentRoutePolyline)
            if (minDistance > OFF_ROUTE_THRESHOLD_METERS) {
                scheduleReroute()
            }
        } else if (previousOrigin == null && _uiState.value.destination != null) {
            requestRoute()
        }
    }

    fun onDestinationSelected(position: Position) {
        _uiState.value = _uiState.value.copy(
            destination = position,
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
            address = "Locating...",
        )

        viewModelScope.launch {
            val result = fetchAddress(position)
            _uiState.value = _uiState.value.copy(address = result)
        }

        requestRoute()
    }

    fun retryRoute() {
        requestRoute()
    }

    fun clearRoute() {
        currentRoutePolyline = emptyList()
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
            _uiState.value = _uiState.value.copy(isLoadingRoute = true, routeError = null)
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
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoute = false,
                        routeError = error.message ?: "Failed to fetch route",
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
