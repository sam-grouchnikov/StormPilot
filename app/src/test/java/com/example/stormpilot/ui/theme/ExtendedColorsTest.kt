package com.example.stormpilot.ui.theme

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExtendedColorsTest : StormPilotUnitTest() {
    @Test
    fun extendedColors_followRequestedDarkMode() {
        val darkColors = stormPilotExtendedColors(darkTheme = true)
        val lightColors = stormPilotExtendedColors(darkTheme = false)

        assertNotEquals(darkColors.exitContainer, lightColors.exitContainer)
        assertNotEquals(darkColors.routingLine, lightColors.routingLine)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFDAD7), lightColors.exitContainer)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF81C3FF), darkColors.routingLine)
    }

    @Test
    fun riskColorsReuseAndDifferentiateAlertPalette() {
        val colors = stormPilotExtendedColors(darkTheme = false)

        assertEquals(colors.alertClearContent, colors.lowRisk)
        assertNotEquals(colors.lowRisk, colors.moderateRisk)
        assertNotEquals(colors.moderateRisk, colors.highRisk)
        assertNotEquals(colors.highRisk, colors.extremeRisk)
    }
}
