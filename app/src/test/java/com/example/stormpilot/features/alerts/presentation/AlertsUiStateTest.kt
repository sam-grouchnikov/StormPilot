package com.example.stormpilot.features.alerts.presentation

import com.example.stormpilot.features.alerts.data.NwsAlert
import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertsUiStateTest : StormPilotUnitTest() {
    @Test
    fun defaultState_hasNoAlertsAndNoLoadingOrError() {
        val state = AlertsUiState()

        assertFalse(state.hasAnyAlert)
        assertEquals(emptyList<NwsAlert>(), state.allAlerts)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun hasAnyAlert_tracksPrimaryAlertSlotsOnly() {
        assertTrue(AlertsUiState(tornadoWarning = alert("Tornado Warning")).hasAnyAlert)
        assertTrue(AlertsUiState(severeThunderstormWatch = alert("Severe Thunderstorm Watch")).hasAnyAlert)
        assertFalse(AlertsUiState(otherAlerts = listOf(alert("Winter Weather Advisory"))).hasAnyAlert)
    }

    @Test
    fun allAlerts_returnsPrimarySlotsInDisplayOrderThenOtherAlerts() {
        val tornadoWarning = alert("Tornado Warning")
        val tornadoWatch = alert("Tornado Watch")
        val flashFloodWarning = alert("Flash Flood Warning")
        val flashFloodWatch = alert("Flash Flood Watch")
        val severeWarning = alert("Severe Thunderstorm Warning")
        val severeWatch = alert("Severe Thunderstorm Watch")
        val other = alert("Winter Weather Advisory")

        val state = AlertsUiState(
            tornadoWarning = tornadoWarning,
            tornadoWatch = tornadoWatch,
            flashFloodWarning = flashFloodWarning,
            flashFloodWatch = flashFloodWatch,
            severeThunderstormWarning = severeWarning,
            severeThunderstormWatch = severeWatch,
            otherAlerts = listOf(other),
        )

        assertEquals(
            listOf(
                tornadoWarning,
                tornadoWatch,
                flashFloodWarning,
                flashFloodWatch,
                severeWarning,
                severeWatch,
                other,
            ),
            state.allAlerts,
        )
    }

    private fun alert(event: String): NwsAlert {
        return NwsAlert(
            id = event,
            event = event,
            headline = event,
            description = event,
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
