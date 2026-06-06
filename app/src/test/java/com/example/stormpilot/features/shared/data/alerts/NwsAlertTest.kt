package com.example.stormpilot.features.shared.data.alerts

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class NwsAlertTest : StormPilotUnitTest() {
    @Test
    fun alertType_classifiesKnownNwsEventsIgnoringCase() {
        assertEquals(AlertType.TORNADO_WARNING, alert("tornado warning").alertType())
        assertEquals(AlertType.TORNADO_WATCH, alert("TORNADO WATCH").alertType())
        assertEquals(AlertType.FLASH_FLOOD_WARNING, alert("Flash Flood Warning").alertType())
        assertEquals(AlertType.FLASH_FLOOD_WATCH, alert("Flash Flood Watch").alertType())
        assertEquals(AlertType.SEVERE_THUNDERSTORM_WARNING, alert("Severe Thunderstorm Warning").alertType())
        assertEquals(AlertType.SEVERE_THUNDERSTORM_WATCH, alert("Severe Thunderstorm Watch").alertType())
        assertEquals(AlertType.OTHER, alert("Winter Weather Advisory").alertType())
    }

    @Test
    fun bestMatchForEvent_returnsNullForBlankEventType() {
        assertNull(listOf(alert("Tornado Warning")).bestMatchForEvent("   "))
    }

    @Test
    fun bestMatchForEvent_prefersExactCaseInsensitiveMatch() {
        val contained = alert("Severe Thunderstorm Warning")
        val exact = alert("Tornado Warning")

        val match = listOf(contained, exact).bestMatchForEvent("tornado warning")

        assertSame(exact, match)
    }

    @Test
    fun bestMatchForEvent_fallsBackToAlertContainingRequestedEvent() {
        val match = listOf(alert("Observed Tornado Warning")).bestMatchForEvent("Tornado Warning")

        assertEquals("Observed Tornado Warning", match?.event)
    }

    @Test
    fun bestMatchForEvent_fallsBackToRequestedEventContainingAlertName() {
        val match = listOf(alert("Tornado Warning")).bestMatchForEvent("Observed Tornado Warning")

        assertEquals("Tornado Warning", match?.event)
    }

    private fun alert(event: String): NwsAlert {
        return NwsAlert(
            id = "id-$event",
            event = event,
            headline = "$event headline",
            description = "$event description",
            instruction = null,
            severity = "Severe",
            urgency = "Immediate",
            effective = null,
            onset = null,
            expires = null,
            areaDescription = null,
            senderName = "NWS",
        )
    }
}
