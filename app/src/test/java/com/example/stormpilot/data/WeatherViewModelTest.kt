package com.example.stormpilot.data

import android.content.Context
import android.location.Geocoder
import app.cash.turbine.test
import com.example.stormpilot.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockContext: Context
    private lateinit var mockLocationRepository: LocationRepository
    private lateinit var mockWeatherRepository: WeatherRepository
    private lateinit var locationFlow: MutableStateFlow<LocationData?>
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        mockContext = mockk()
        mockLocationRepository = mockk(relaxed = true)
        mockWeatherRepository = mockk()
        
        locationFlow = MutableStateFlow(null)
        every { mockLocationRepository.location } returns locationFlow

        // Geocoder is created inside reverseGeocode, mock it
        mockkConstructor(Geocoder::class)
    }

    @After
    fun teardown() {
        unmockkAll()
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun testInitialState() = runTest {
        viewModel = WeatherViewModel(mockContext, mockLocationRepository, mockWeatherRepository)
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.cityName.test {
            val name = awaitItem()
            assertEquals("Locating...", name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testLocationUpdateTriggersWeatherFetchSuccess() = runTest {
        val snapshot = WeatherSnapshot(
            current = CurrentWeather(70, "Clear", null, null, null, null),
            hourly = emptyList(),
            daily = emptyList(),
            stormSpecs = emptyList()
        )
        
        coEvery { mockWeatherRepository.fetchWeather(any(), any()) } returns Result.success(snapshot)
        
        viewModel = WeatherViewModel(mockContext, mockLocationRepository, mockWeatherRepository)

        // Trigger a location update
        locationFlow.value = LocationData(latitude = 37.0, longitude = -122.0, bearing = 0f, speed = 0f)

        viewModel.uiState.test {
            // First item might be the initial loading state
            val item1 = awaitItem()
            
            // Skip loading states until we get the success state
            var finalState = item1
            while (finalState.isLoading) {
                finalState = awaitItem()
            }
            
            assertFalse(finalState.isLoading)
            assertEquals("Clear", finalState.current?.conditions)
            assertEquals(70, finalState.current?.temperature)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

}
