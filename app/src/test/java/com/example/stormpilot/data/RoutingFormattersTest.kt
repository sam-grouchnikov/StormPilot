package com.example.stormpilot.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutingFormattersTest {

    @Test
    fun testFormatDistanceInMiles() {
        // 1609.344 meters is 1.0 mile
        assertEquals("1.0 mi", formatDistance(1609.344))
        
        // 3218.688 meters is 2.0 miles
        assertEquals("2.0 mi", formatDistance(3218.688))
        
        // 500 meters is approx 0.31 miles, which is >= 0.2
        assertEquals("0.3 mi", formatDistance(500.0))
        
        // 321.8688 meters is exactly 0.2 miles, which should format as miles
        assertEquals("0.2 mi", formatDistance(321.87))
    }

    @Test
    fun testFormatDistanceInMeters() {
        // Less than 0.2 miles (approx 321.8 meters)
        // 300 meters is less than 0.2 miles (approx 0.186 miles)
        assertEquals("300 m", formatDistance(300.0))
        
        // 100 meters is less than 0.2 miles
        assertEquals("100 m", formatDistance(100.0))
        
        // 0 meters
        assertEquals("0 m", formatDistance(0.0))
    }

    @Test
    fun testFormatDurationInMinutes() {
        // Less than 60 minutes
        assertEquals("0 min", formatDuration(0.0))
        assertEquals("1 min", formatDuration(30.0)) // rounds up to 1 min (0.5 min)
        assertEquals("59 min", formatDuration(3540.0)) // 59 minutes
    }

    @Test
    fun testFormatDurationInExactHours() {
        // Exact hours (no remaining minutes)
        assertEquals("1 hr", formatDuration(3600.0)) // 60 minutes -> 1 hr
        assertEquals("2 hr", formatDuration(7200.0)) // 120 minutes -> 2 hr
    }

    @Test
    fun testFormatDurationInHoursAndMinutes() {
        // Hours and remaining minutes
        assertEquals("1 hr 5 min", formatDuration(3900.0)) // 65 minutes -> 1 hr 5 min
        assertEquals("2 hr 30 min", formatDuration(9000.0)) // 150 minutes -> 2 hr 30 min
    }
}
