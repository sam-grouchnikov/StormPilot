package com.example.stormpilot.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NwsAlertTest {

    private fun createAlert(event: String): NwsAlert {
        return NwsAlert(
            id = "test-id",
            event = event,
            headline = "Test Headline",
            description = "Test Description",
            instruction = "Test Instruction",
            severity = "Severe",
            urgency = "Immediate",
            onset = null,
            expires = null,
            senderName = "NWS"
        )
    }

    @Test
    fun testAlertTypes() {
        assertEquals(AlertType.TORNADO_WARNING, createAlert("Tornado Warning").alertType())
        assertEquals(AlertType.TORNADO_WATCH, createAlert("Tornado Watch").alertType())
        assertEquals(AlertType.FLASH_FLOOD_WARNING, createAlert("Flash Flood Warning").alertType())
        assertEquals(AlertType.FLASH_FLOOD_WATCH, createAlert("Flash Flood Watch").alertType())
        assertEquals(AlertType.SEVERE_THUNDERSTORM_WARNING, createAlert("Severe Thunderstorm Warning").alertType())
        assertEquals(AlertType.SEVERE_THUNDERSTORM_WATCH, createAlert("Severe Thunderstorm Watch").alertType())
        assertEquals(AlertType.OTHER, createAlert("Blizzard Warning").alertType())
    }

    @Test
    fun testAlertTypeCaseInsensitivity() {
        assertEquals(AlertType.TORNADO_WARNING, createAlert("tornado warning").alertType())
        assertEquals(AlertType.TORNADO_WATCH, createAlert("TORNADO WATCH").alertType())
        assertEquals(AlertType.FLASH_FLOOD_WARNING, createAlert("fLaSh FlOoD wArNiNg").alertType())
    }

    @Test
    fun testAlertTypeMixedContent() {
        assertEquals(AlertType.SEVERE_THUNDERSTORM_WARNING, createAlert("A severe Severe Thunderstorm Warning is in effect").alertType())
        assertEquals(AlertType.OTHER, createAlert("No warnings active").alertType())
    }
}
