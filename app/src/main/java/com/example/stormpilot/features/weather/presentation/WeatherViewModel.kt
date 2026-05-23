package com.example.stormpilot.features.weather.presentation

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.features.location.data.LocationRepository
import com.example.stormpilot.features.weather.data.CurrentWeather
import com.example.stormpilot.features.weather.data.DailyWeatherOutlook
import com.example.stormpilot.features.weather.data.HourlyForecast
import com.example.stormpilot.features.weather.data.StormSpec
import com.example.stormpilot.features.weather.data.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
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
    @ApplicationContext private val context: Context,
    val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _cityName = MutableStateFlow("Locating...")
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private var fetchJob: Job? = null

    init {
        locationRepository.startTracking()
        observeLocationForWeather()
        observeLocationForCityName()
        startPeriodicRefresh()
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
                    _cityName.value = reverseGeocode(location.latitude, location.longitude)
                        ?: "Current Location"
                }
        }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(10 * 60 * 1000L)
                locationRepository.location.value?.let { location ->
                    fetchWeather(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
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

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val address = Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                listOfNotNull(address?.locality, address?.adminArea)
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { null }
            } catch (e: Exception) {
                null
            }
        }

    override fun onCleared() {
        super.onCleared()
        locationRepository.stopTracking()
    }
}
