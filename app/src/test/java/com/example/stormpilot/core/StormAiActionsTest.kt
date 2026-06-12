package com.example.stormpilot.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StormAiActionsTest {
    @Test
    fun parseStormAiResponse_parsesSetDestinationAction() {
        val response = parseStormAiResponse(
            """
            {
              "reply": "Routing to Norman.",
              "actions": [
                {
                  "type": "SET_DESTINATION",
                  "id": "destination-1",
                  "label": "Norman, OK",
                  "latitude": 35.2226,
                  "longitude": -97.4395,
                  "address": "Norman, Oklahoma",
                  "confidence": 0.94
                }
              ]
            }
            """.trimIndent(),
        )

        val action = response.actions.single()
        assertTrue(action is StormAiAction.SetDestination)
        action as StormAiAction.SetDestination
        assertEquals("Routing to Norman.", response.reply)
        assertEquals("destination-1", action.id)
        assertEquals("Norman, OK", action.label)
        assertEquals(35.2226, action.latitude, 0.0001)
        assertEquals(-97.4395, action.longitude, 0.0001)
        assertEquals("Norman, Oklahoma", action.address)
        assertEquals(0.94, action.confidence ?: 0.0, 0.0001)
    }

    @Test
    fun parseStormAiResponse_parsesConfirmationWithPendingAction() {
        val response = parseStormAiResponse(
            """
            {
              "reply": "That route crosses warning polygons.",
              "actions": [
                {
                  "type": "REQUEST_CONFIRMATION",
                  "message": "Start this route anyway?",
                  "confirmLabel": "Start",
                  "cancelLabel": "Cancel",
                  "riskSummary": "One active warning intersects the route.",
                  "pendingActions": [
                    {
                      "type": "PREVIEW_ROUTE",
                      "label": "Western edge route",
                      "destination": {
                        "label": "El Reno",
                        "latitude": 35.5323,
                        "longitude": -97.9550
                      },
                      "warningCount": 1
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
        )

        val confirmation = response.actions.single()
        assertTrue(confirmation is StormAiAction.RequestConfirmation)
        confirmation as StormAiAction.RequestConfirmation
        val preview = confirmation.pendingActions.single()
        assertTrue(preview is StormAiAction.PreviewRoute)
        preview as StormAiAction.PreviewRoute
        assertEquals("Start this route anyway?", confirmation.message)
        assertEquals("Start", confirmation.confirmLabel)
        assertEquals("Western edge route", preview.label)
        assertEquals("El Reno", preview.destination.label)
        assertEquals(1, preview.warningCount)
    }

    @Test
    fun parseStormAiResponse_rejectsUnknownActionType() {
        try {
            parseStormAiResponse(
                """
                {
                  "reply": "Nope.",
                  "actions": [
                    { "type": "TELEPORT_MAP" }
                  ]
                }
                """.trimIndent(),
            )
        } catch (expected: IllegalArgumentException) {
            return
        }
        throw AssertionError("Expected IllegalArgumentException")
    }
}
