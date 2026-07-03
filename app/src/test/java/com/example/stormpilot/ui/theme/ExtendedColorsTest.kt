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

        assertNotEquals(darkColors.navigation.exitButtonContainer, lightColors.navigation.exitButtonContainer)
        assertNotEquals(darkColors.navigation.routeLine, lightColors.navigation.routeLine)
        assertNotEquals(darkColors.theme.vibrantPrimary, lightColors.theme.vibrantPrimary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFDAD7), lightColors.navigation.exitButtonContainer)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF81C3FF), darkColors.navigation.routeLine)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF7C4FD9), lightColors.theme.vibrantPrimary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFB291F3), darkColors.theme.vibrantPrimary)
    }

    @Test
    fun riskColorsReuseAndDifferentiateAlertPalette() {
        val colors = stormPilotExtendedColors(darkTheme = false)

        assertEquals(colors.alerts.clearContent, colors.weather.stormRiskLow)
        assertNotEquals(colors.weather.stormRiskLow, colors.weather.stormRiskModerate)
        assertNotEquals(colors.weather.stormRiskModerate, colors.weather.stormRiskHigh)
        assertNotEquals(colors.weather.stormRiskHigh, colors.weather.stormRiskExtreme)
    }
}
