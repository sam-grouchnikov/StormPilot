package com.example.stormpilot.features.shared.viewmodels

import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.DailyWeatherOutlook
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.data.weather.StormSpec
import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUiStateTest : StormPilotUnitTest() {
    @Test
    fun defaultState_isIdleAndEmpty() {
        val state = WeatherUiState()

        assertNull(state.current)
        assertEquals(emptyList<HourlyForecast>(), state.hourly)
        assertEquals(emptyList<DailyWeatherOutlook>(), state.daily)
        assertEquals(emptyList<StormSpec>(), state.stormSpecs)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun copyCanRepresentLoadedWeatherSnapshot() {
        val current = CurrentWeather(
            temperature = 72,
            conditions = "Thunderstorms",
            dewPoint = 68,
            humidity = 80,
            windSpeed = 25,
            windGust = 40,
        )
        val hourly = listOf(HourlyForecast("14:00", 73, "Rain", 80))
        val daily = listOf(DailyWeatherOutlook("Today", "Severe storms", 78, 65, "Enhanced"))
        val stormSpecs = listOf(StormSpec("CAPE", "2500 J/kg", "Instability"))

        val state = WeatherUiState(isLoading = true).copy(
            current = current,
            hourly = hourly,
            daily = daily,
            stormSpecs = stormSpecs,
            isLoading = false,
            error = null,
        )

        assertEquals(current, state.current)
        assertEquals(hourly, state.hourly)
        assertEquals(daily, state.daily)
        assertEquals(stormSpecs, state.stormSpecs)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun errorStateCanRetainPreviousWeatherWhileNotLoading() {
        val current = CurrentWeather(
            temperature = 60,
            conditions = "Clear",
            dewPoint = null,
            humidity = null,
            windSpeed = null,
            windGust = null,
        )

        val state = WeatherUiState(current = current).copy(
            isLoading = false,
            error = "Weather data could not be loaded.",
        )

        assertEquals(current, state.current)
        assertTrue(state.error!!.contains("could not be loaded"))
        assertFalse(state.isLoading)
    }
}
