package com.example.stormpilot.features.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.features.shared.data.alerts.AlertType
import com.example.stormpilot.features.shared.data.alerts.AlertsRepository
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import com.example.stormpilot.features.shared.data.alerts.alertType
import com.example.stormpilot.features.shared.data.location.LocationLookupRepository
import com.example.stormpilot.features.shared.data.location.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.maplibre.compose.sources.GeoJsonData
import kotlin.math.abs

data class AlertsUiState(
    val tornadoWarning: NwsAlert? = null,
    val tornadoWatch: NwsAlert? = null,
    val flashFloodWarning: NwsAlert? = null,
    val flashFloodWatch: NwsAlert? = null,
    val severeThunderstormWarning: NwsAlert? = null,
    val severeThunderstormWatch: NwsAlert? = null,
    val otherAlerts: List<NwsAlert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val hasAnyAlert get() = tornadoWarning != null
            || tornadoWatch != null
            || flashFloodWarning != null
            || flashFloodWatch != null
            || severeThunderstormWarning != null
            || severeThunderstormWatch != null

    val allAlerts: List<NwsAlert>
        get() = buildList {
            listOfNotNull(
                tornadoWarning,
                tornadoWatch,
                flashFloodWarning,
                flashFloodWatch,
                severeThunderstormWarning,
                severeThunderstormWatch,
            ).forEach(::add)
            addAll(otherAlerts)
        }
}

/**
 * Tracks location-aware NWS alerts, groups them by severity family, and provides the current display city.
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    val locationRepository: LocationRepository,
    private val alertsRepository: AlertsRepository,
    private val locationLookupRepository: LocationLookupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()
    private val _cityName = MutableStateFlow("Locating...")
    val cityName: StateFlow<String> = _cityName.asStateFlow()
    private val _locationGeoJson = MutableStateFlow(GeoJsonData.JsonString(EMPTY_FEATURE_COLLECTION))
    val locationGeoJson: StateFlow<GeoJsonData> = _locationGeoJson.asStateFlow()

    private var fetchJob: Job? = null
    private var useDashboardPlaceholderData = false

    init {
        observeLocationForMapOverlay()
        observeLocationChanges()
        startPeriodicRefresh()
        observeLocationForCityName()
    }

    fun setDashboardPlaceholderMode(enabled: Boolean) {
        if (enabled == useDashboardPlaceholderData) return

        useDashboardPlaceholderData = enabled
        if (enabled) {
            fetchJob?.cancel()
            locationRepository.stopTracking()
            _uiState.value = AlertsUiState(isLoading = false)
            _cityName.value = "Offline Sample"
            _locationGeoJson.value = GeoJsonData.JsonString(EMPTY_FEATURE_COLLECTION)
        } else {
            _cityName.value = "Locating..."
            locationRepository.location.value?.let { loc ->
                fetchAlerts(loc.latitude, loc.longitude)
            }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        locationLookupRepository.reverseLocation(latitude, longitude)
            .getOrNull()
            ?.name
            ?.takeIf { it.isNotBlank() }

    private fun observeLocationForCityName() {
        viewModelScope.launch {
            locationRepository.location
                .filterNotNull()
                .distinctUntilChanged { old, new ->
                    abs(old.latitude - new.latitude) < 0.01 &&
                            abs(old.longitude - new.longitude) < 0.01
                }
                .collectLatest { loc ->
                    if (useDashboardPlaceholderData) return@collectLatest
                    val name = reverseGeocode(loc.latitude, loc.longitude)
                    _cityName.value = name ?: "Unknown Area"
                }
        }
    }

    private fun observeLocationForMapOverlay() {
        viewModelScope.launch {
            locationRepository.location
                .filterNotNull()
                .collect { loc ->
                    if (useDashboardPlaceholderData) return@collect
                    _locationGeoJson.value = GeoJsonData.JsonString(
                        """
                        {
                          "type": "FeatureCollection",
                          "features": [{
                            "type": "Feature",
                            "geometry": {
                              "type": "Point",
                              "coordinates": [${loc.longitude}, ${loc.latitude}]
                            },
                            "properties": {}
                          }]
                        }
                        """.trimIndent()
                    )
                }
        }
    }

    // Re-fetch alerts when the user moves significantly (0.05 degrees ≈ 5km)
    private fun observeLocationChanges() {
        viewModelScope.launch {
            locationRepository.location
                .map { loc ->
                    loc?.let {
                        Pair(
                            Math.round(it.latitude * 20) / 20.0,
                            Math.round(it.longitude * 20) / 20.0,
                        )
                    }
                }
                .distinctUntilChanged()
                .collect { truncatedLocation ->
                    if (useDashboardPlaceholderData) return@collect
                    truncatedLocation?.let { _ ->
                        // Use the actual raw location, not the truncated one
                        locationRepository.location.value?.let { loc ->
                            fetchAlerts(loc.latitude, loc.longitude)
                        }
                    }
                }
        }
    }


    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(1 * 1 * 1000L)
                if (useDashboardPlaceholderData) continue
                locationRepository.location.value?.let { loc ->
                    fetchAlerts(loc.latitude, loc.longitude)
                }
            }
        }
    }

    private fun fetchAlerts(latitude: Double, longitude: Double) {
        if (useDashboardPlaceholderData) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            alertsRepository.fetchAlerts(latitude, longitude)
                .onSuccess { alerts ->
                    _uiState.value = AlertsUiState(
                        tornadoWarning = alerts.firstOrNull {
                            it.alertType() == AlertType.TORNADO_WARNING
                        },
                        tornadoWatch = alerts.firstOrNull {
                            it.alertType() == AlertType.TORNADO_WATCH
                        },
                        flashFloodWarning = alerts.firstOrNull {
                            it.alertType() == AlertType.FLASH_FLOOD_WARNING
                        },
                        flashFloodWatch = alerts.firstOrNull {
                            it.alertType() == AlertType.FLASH_FLOOD_WATCH
                        },
                        severeThunderstormWarning = alerts.firstOrNull {
                            it.alertType() == AlertType.SEVERE_THUNDERSTORM_WARNING
                        },
                        severeThunderstormWatch = alerts.firstOrNull {
                            it.alertType() == AlertType.SEVERE_THUNDERSTORM_WATCH
                        },
                        otherAlerts = alerts.filter {
                            it.alertType() == AlertType.OTHER
                        },
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                    )
                }
        }
    }



    override fun onCleared() {
        super.onCleared()
        locationRepository.stopTracking()
    }

    private companion object {
        private const val EMPTY_FEATURE_COLLECTION = """{"type":"FeatureCollection","features":[]}"""
    }
}
