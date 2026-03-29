package com.example.stormpilot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.data.AlertType
import com.example.stormpilot.data.AlertsRepository
import com.example.stormpilot.data.LocationRepository
import com.example.stormpilot.data.NwsAlert
import com.example.stormpilot.data.alertType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    val locationRepository = LocationRepository(application)
    private val alertsRepository = AlertsRepository()

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState

    private var fetchJob: Job? = null

    init {
        locationRepository.startTracking()
        observeLocationChanges()
        startPeriodicRefresh()
    }

    // Re-fetch alerts when the user moves significantly (0.05 degrees ≈ 5km)
    private fun observeLocationChanges() {
        viewModelScope.launch {
            locationRepository.location
                .map { loc ->
                    loc?.let {
                        // Truncate to 2 decimal places to avoid fetching on every tiny movement
                        Pair(
                            Math.round(it.latitude * 20) / 20.0,
                            Math.round(it.longitude * 20) / 20.0,
                        )
                    }
                }
                .distinctUntilChanged()
                .collect { truncatedLocation ->
                    truncatedLocation?.let { (lat, lon) ->
                        fetchAlerts(lat, lon)
                    }
                }
        }
    }

    // Also refresh every 5 minutes regardless of movement
    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L)
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