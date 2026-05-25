package com.example.stormpilot.core

import androidx.appcompat.app.AppCompatDelegate
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppSettingsTest {

    @Before
    fun setup() {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testModeInitialState() {
        assertEquals("system", AppSettings.mode)
    }

    @Test
    fun testUpdateAppCompatNightModeLight() {
        updateAppCompatNightMode("light")
        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
    }

    @Test
    fun testUpdateAppCompatNightModeDark() {
        updateAppCompatNightMode("dark")
        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
    }

    @Test
    fun testUpdateAppCompatNightModeSystem() {
        updateAppCompatNightMode("system")
        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
    }

    @Test
    fun testUpdateAppCompatNightModeUnknownDefaultsToSystem() {
        updateAppCompatNightMode("unknown")
        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
    }
}
