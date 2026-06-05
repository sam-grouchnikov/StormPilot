package com.example.stormpilot.ui.theme

import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExtendedColorsTest : StormPilotUnitTest() {
    @After
    fun resetDarkMode() {
        AppSettings.isDarkMode = true
    }

    @Test
    fun extendedColors_followCurrentDarkModeFlag() {
        val colors = ExtendedColors()

        AppSettings.isDarkMode = true
        val darkExitContainer = colors.exitContainer
        val darkRouteLine = colors.routingLine

        AppSettings.isDarkMode = false
        val lightExitContainer = colors.exitContainer
        val lightRouteLine = colors.routingLine

        assertNotEquals(darkExitContainer, lightExitContainer)
        assertNotEquals(darkRouteLine, lightRouteLine)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFDAD7), lightExitContainer)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF81C3FF), darkRouteLine)
    }

    @Test
    fun riskColorsReuseAndDifferentiateAlertPalette() {
        val colors = ExtendedColors()

        AppSettings.isDarkMode = false

        assertEquals(colors.alertClearContent, colors.lowRisk)
        assertNotEquals(colors.lowRisk, colors.moderateRisk)
        assertNotEquals(colors.moderateRisk, colors.highRisk)
        assertNotEquals(colors.highRisk, colors.extremeRisk)
    }
}
