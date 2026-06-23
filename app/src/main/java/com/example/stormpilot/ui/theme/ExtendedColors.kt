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

    public val alertRadarClearContainer: Color
        get() = if (isDark) Color(0xFF212623) else Color(0xFFE7F7EA)

    public val alertRadarClearOutline: Color
        get() = if (isDark) Color(0xFF3B8348) else Color(0xFF3C8B4C)

    public val alertRadarClearContent: Color
        get() = if (isDark) Color(0xFF9BDBA7) else Color(0xFF205C2C)

    public val alertRadarActiveContainer: Color
        get() = if (isDark) Color(0xFF2F1E1E) else Color(0xFFE4EAF6)

    public val alertRadarActiveContent: Color
        get() = alertWarningContent

    public val blueBackground: Color
        get() = if (isDark) Color(0xFF252538) else Color(0xFFE4EAF6)

    public val welcomeNavyStart: Color
        get() = Color(0xFF030C1A)

    public val welcomeNavyEnd: Color
        get() = Color(0xFF08203B)

    public val purpleBackground: Color
        get() = if (isDark) Color(0xFF312538) else Color(0xFFEFE8F6)

    public val routingLine: Color
        get() = if (isDark) Color(0xFF81C3FF) else Color(0xFF0046D9)

    public val routingHeader: Color
        get() = if (isDark) Color(0xFF175646) else Color(0xFF296C59)

    public val greenPrimary: Color
        get() = if (isDark) Color(0xFFD8BDE4) else Color(0xFF6C5677)

    public val purplePrimaryContainer: Color
        get() = if (isDark) Color(0xFF5A396E) else Color(0xFFDCBCEA)

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


    public val gasBox: Color
        get() = if (isDark) Color(0xFF3D2A1A) else Color(0xFFFDF0E6)

    public val gasOutline: Color
        get() = if (isDark) Color(0xFFBF7B3A) else Color(0xFFD4854A)

    public val gasContent: Color
        get() = if (isDark) Color(0xFFFFB77A) else Color(0xFF7B420F)

    public val groceryBox: Color
        get() = if (isDark) Color(0xFF173821) else Color(0xFFE7F7EA)

    public val groceryOutline: Color
        get() = if (isDark) Color(0xFF3B8348) else Color(0xFF3C8B4C)

    public val groceryContent: Color
        get() = if (isDark) Color(0xFF9BDBA7) else Color(0xFF205C2C)

    public val hotelBox: Color
        get() = if (isDark) Color(0xFF172C48) else Color(0xFFE5F0FF)

    public val hotelOutline: Color
        get() = if (isDark) Color(0xFF4172BD) else Color(0xFF4377D6)

    public val hotelContent: Color
        get() = if (isDark) Color(0xFFA9CAFF) else Color(0xFF1F4F9F)

    public val stormAIOutline: Color
        get() = if (isDark) Color(0xFF4D318F) else Color(0xFF623ABF)

    public val locationCircle: Color
        get() = if (isDark) Color(0xFF364EFF) else Color(0xFF1B36FF)

}
