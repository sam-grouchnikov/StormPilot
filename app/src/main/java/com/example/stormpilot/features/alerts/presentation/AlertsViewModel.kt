package com.example.stormpilot.features.alerts.presentation

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.features.alerts.data.AlertType
import com.example.stormpilot.features.alerts.data.AlertsRepository
import com.example.stormpilot.features.location.data.LocationRepository
import com.example.stormpilot.features.alerts.data.NwsAlert
import com.example.stormpilot.features.alerts.data.alertType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
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
}

/**
 * Tracks location-aware NWS alerts, groups them by severity family, and provides the current display city.
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val locationRepository: LocationRepository,
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState
    private val _cityName = MutableStateFlow("Locating...")
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private var fetchJob: Job? = null

    init {
        locationRepository.startTracking()
        observeLocationChanges()
        startPeriodicRefresh()
        observeLocationForCityName()
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()

                if (address != null) {
                    "${address.locality}, ${address.adminArea}"
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun observeLocationForCityName() {
        viewModelScope.launch {
            locationRepository.location
                .filterNotNull()
                .distinctUntilChanged { old, new ->
                    abs(old.latitude - new.latitude) < 0.01 &&
                            abs(old.longitude - new.longitude) < 0.01
                }
                .collectLatest { loc ->
                    val name = withContext(Dispatchers.IO) {
                        reverseGeocode(loc.latitude, loc.longitude)
                    }
                    _cityName.value = name ?: "Unknown Area"
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
                locationRepository.location.value?.let { loc ->
                    fetchAlerts(loc.latitude, loc.longitude)
                }
            }
        }
    }

    private fun fetchAlerts(latitude: Double, longitude: Double) {
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
}
