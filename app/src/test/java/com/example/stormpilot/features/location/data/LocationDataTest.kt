package com.example.stormpilot.features.location.data

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDataTest : StormPilotUnitTest() {
    @Test
    fun locationData_defaultsBearingAndSpeedToZero() {
        val location = LocationData(latitude = 35.0, longitude = -97.0)

        assertEquals(35.0, location.latitude, 0.0)
        assertEquals(-97.0, location.longitude, 0.0)
        assertEquals(0f, location.bearing)
        assertEquals(0f, location.speed)
    }

    @Test
    fun locationData_copyPreservesImmutabilitySemantics() {
        val original = LocationData(latitude = 35.0, longitude = -97.0, bearing = 90f, speed = 12f)
        val moved = original.copy(latitude = 36.0)

        assertEquals(35.0, original.latitude, 0.0)
        assertEquals(36.0, moved.latitude, 0.0)
        assertEquals(original.longitude, moved.longitude, 0.0)
        assertEquals(original.bearing, moved.bearing)
        assertEquals(original.speed, moved.speed)
    }
}
