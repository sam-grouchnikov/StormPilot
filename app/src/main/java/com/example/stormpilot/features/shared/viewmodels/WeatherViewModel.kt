package com.example.stormpilot.features.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.features.shared.data.location.LocationLookupRepository
import com.example.stormpilot.features.shared.data.location.LocationRepository
import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.DashboardWeatherPlaceholderRepository
import com.example.stormpilot.features.shared.data.weather.DailyWeatherOutlook
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.data.weather.StormSpec
import com.example.stormpilot.features.shared.data.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
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
import kotlin.math.round

data class WeatherUiState(
    val current: CurrentWeather? = null,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyWeatherOutlook> = emptyList(),
    val stormSpecs: List<StormSpec> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * Bridges live location updates to weather fetches and exposes current, hourly, daily, and storm-spec UI state.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val dashboardWeatherPlaceholderRepository: DashboardWeatherPlaceholderRepository,
    private val locationLookupRepository: LocationLookupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _cityName = MutableStateFlow("Locating...")
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private var fetchJob: Job? = null
    private var useDashboardPlaceholderData = false
    private var dashboardPlaceholderAssetName: String? = null

    init {
        observeLocationForWeather()
        observeLocationForCityName()
        startPeriodicRefresh()
    }

    fun setDashboardPlaceholderSource(enabled: Boolean, assetName: String) {
        val sourceUnchanged = enabled == useDashboardPlaceholderData &&
            (!enabled || dashboardPlaceholderAssetName == assetName)
        if (sourceUnchanged) return

        useDashboardPlaceholderData = enabled
        dashboardPlaceholderAssetName = assetName.takeIf { enabled }

        if (enabled) {
            fetchJob?.cancel()
            locationRepository.stopTracking()
            loadDashboardPlaceholder(assetName)
        } else {
            _cityName.value = "Locating..."
            _uiState.value = WeatherUiState(isLoading = true)
            locationRepository.location.value?.let { location ->
                fetchWeather(location.latitude, location.longitude)
            }
        }
    }

    private fun observeLocationForWeather() {
        viewModelScope.launch {
            locationRepository.location
                .map { location ->
                    location?.let {
                        Pair(
                            round(it.latitude * 20) / 20.0,
                            round(it.longitude * 20) / 20.0,
                        )
                    }
                }
                .distinctUntilChanged()
                .collect { locationKey ->
                    if (useDashboardPlaceholderData) return@collect
                    locationKey ?: return@collect
                    locationRepository.location.value?.let { location ->
                        fetchWeather(location.latitude, location.longitude)
                    }
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
                .collectLatest { location ->
                    if (useDashboardPlaceholderData) return@collectLatest
                    _cityName.value = reverseGeocode(location.latitude, location.longitude)
                        ?: "Current Location"
                }
        }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(10 * 60 * 1000L)
                if (useDashboardPlaceholderData) continue
                locationRepository.location.value?.let { location ->
                    fetchWeather(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        if (useDashboardPlaceholderData) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            weatherRepository.fetchWeather(latitude, longitude)
                .onSuccess { snapshot ->
                    _uiState.value = WeatherUiState(
                        current = snapshot.current,
                        hourly = snapshot.hourly,
                        daily = snapshot.daily,
                        stormSpecs = snapshot.stormSpecs,
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Weather data could not be loaded.",
                    )
                }
        }
    }

    private fun loadDashboardPlaceholder(assetName: String) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = WeatherUiState(isLoading = true)
            dashboardWeatherPlaceholderRepository.load(assetName)
                .onSuccess { placeholder ->
                    _cityName.value = placeholder.cityName
                    _uiState.value = WeatherUiState(
                        current = placeholder.snapshot.current,
                        hourly = placeholder.snapshot.hourly,
                        daily = placeholder.snapshot.daily,
                        stormSpecs = placeholder.snapshot.stormSpecs,
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    _cityName.value = "Offline Sample"
                    _uiState.value = WeatherUiState(
                        isLoading = false,
                        error = error.localizedMessage ?: "Dashboard placeholder data could not be loaded.",
                    )
                }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        locationLookupRepository.reverseLocation(latitude, longitude)
            .getOrNull()
            ?.name
            ?.takeIf { it.isNotBlank() }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopTracking()
    }
}
