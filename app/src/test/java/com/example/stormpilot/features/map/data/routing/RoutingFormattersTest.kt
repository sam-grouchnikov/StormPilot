package com.example.stormpilot.features.map.data.routing

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class RoutingFormattersTest : StormPilotUnitTest() {
    @Test
    fun formatDistance_usesMetersBelowPointTwoMiles() {
        assertEquals("100 m", formatDistance(100.4))
        assertEquals("321 m", formatDistance(321.0))
    }

    @Test
    fun formatDistance_usesMilesAtPointTwoMilesAndAbove() {
        Locale.setDefault(Locale.US)

        assertEquals("0.2 mi", formatDistance(321.8688))
        assertEquals("1.5 mi", formatDistance(2_414.016))
    }

    @Test
    fun formatDuration_usesMinutesUnderOneHour() {
        assertEquals("0 min", formatDuration(0.0))
        assertEquals("2 min", formatDuration(91.0))
        assertEquals("59 min", formatDuration(3_540.0))
    }

    @Test
    fun formatDuration_usesHoursAndRemainingMinutesAtOneHourAndAbove() {
        assertEquals("1 hr", formatDuration(3_600.0))
        assertEquals("1 hr 30 min", formatDuration(5_400.0))
        assertEquals("2 hr 1 min", formatDuration(7_260.0))
    }
}
