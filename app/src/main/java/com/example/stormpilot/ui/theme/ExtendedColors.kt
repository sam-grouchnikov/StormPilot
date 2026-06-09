package com.example.stormpilot.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.stormpilot.core.AppSettings

public class ExtendedColors {
    public val isDark: Boolean
        get() = AppSettings.isDarkMode

    public val exitContainer: Color
        get() = if (isDark) Color(0xFF761A12) else Color(0xFFFFDAD7)

    public val exitText: Color
        get() = if (isDark) Color(0xFFFFBEB7) else Color(0xFFBA1A1A)

    public val alertClearContent: Color
        get() = if (isDark) Color(0xFF93AD99) else Color(0xFF386A42)

    public val alertClearContentAlternate: Color
        get() = if (isDark) Color(0xFF62A26F) else Color(0xFF1E4620)

    public val alertWatchContainer: Color
        get() = if (isDark) Color(0xFF5A4212) else Color(0xFFFFDF9E)

    public val alertWatchContent: Color
        get() = if (isDark) Color(0xFFFFE09A) else Color(0xFF7B5A00)

    public val alertWarningContainer: Color
        get() = if (isDark) Color(0xFF761A12) else Color(0xFFFFDAD6)

    public val alertWarningContent: Color
        get() = if (isDark) Color(0xFFFFBEB7) else Color(0xFF93000A)

    public val blueBackground: Color
        get() = if (isDark) Color(0xFF252538) else Color(0xFFE4EAF6)

    public val purpleBackground: Color
        get() = if (isDark) Color(0xFF312538) else Color(0xFFEFE8F6)

    public val routingLine: Color
        get() = if (isDark) Color(0xFF81C3FF) else Color(0xFF0046D9)

    public val routingHeader: Color
        get() = if (isDark) Color(0xFF175646) else Color(0xFF296C59)

    public val greenPrimary: Color
        get() = if (isDark) Color(0xFFD8BDE4) else Color(0xFF6C5677)

    public val purplePrimaryContainer: Color
        get() = if (isDark) Color(0xFF5A396E) else Color(0xFFEAC8F8)

    public val purpleSurfaceContainer: Color
        get() = if (isDark) Color(0xFF151115) else Color(0xFFF8F8F8)

    public val purpleShadow: Color
        get() = if (isDark) Color(0xFF1D1A20) else Color(0xFFD7D1DC)

    public val searchBarColor: Color
        get() = if (isDark) Color(0xFF131618) else Color(0xFFFFFFFF)

    public val iconButtonColor: Color
        get() = if (isDark) Color(0xFF131618) else Color(0xFFFFFFFF)


    public val stormContainer: Color
        get() = if (isDark) Color(0xFF412121) else Color(0xFFFFDAD9)

    public val stormText: Color
        get() = if (isDark) Color(0xFFFFD0D0) else Color(0xFF410006)

    public val stormTextNested: Color
        get() = if (isDark) Color(0xFFFFB3B3) else Color(0xFF900C3F)

    public val stormContainerNested: Color
        get() = if (isDark) Color(0xFF503737) else Color(0xFFF5EEEE)

    public val profileIcon: Color
        get() = if (isDark) Color(0xFF5D8F33) else Color(0xFF679F38)


    public val lowRisk: Color
        get() = alertClearContent
    public val moderateRisk: Color
        get() = if (isDark) Color(0xFFACAD93) else Color(0xFF6B6E40)
    public val highRisk: Color
        get() = if (isDark) Color(0xFFAD9D93) else Color(0xFF7A573F)
    public val extremeRisk: Color
        get() = if (isDark) Color(0xFFA993AD) else Color(0xFF6F4B78)

}