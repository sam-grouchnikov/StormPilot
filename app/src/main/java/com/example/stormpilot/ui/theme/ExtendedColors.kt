package com.example.stormpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
public data class ExtendedColors(
    public val isDark: Boolean,
    public val exitContainer: Color,
    public val exitText: Color,
    public val alertClearContent: Color,
    public val alertClearContentAlternate: Color,
    public val alertWatchContainer: Color,
    public val alertWatchContent: Color,
    public val alertWarningContainer: Color,
    public val alertWarningContent: Color,
    public val alertRadarClearContainer: Color,
    public val alertRadarClearOutline: Color,
    public val alertRadarClearContent: Color,
    public val alertRadarActiveContainer: Color,
    public val blueBackground: Color,
    public val welcomeNavyStart: Color,
    public val welcomeNavyEnd: Color,
    public val purpleBackground: Color,
    public val routingLine: Color,
    public val routingHeader: Color,
    public val greenPrimary: Color,
    public val purplePrimaryContainer: Color,
    public val purpleSurfaceContainer: Color,
    public val purpleShadow: Color,
    public val searchBarColor: Color,
    public val iconButtonColor: Color,
    public val stormContainer: Color,
    public val stormText: Color,
    public val stormTextNested: Color,
    public val stormContainerNested: Color,
    public val profileIcon: Color,
    public val moderateRisk: Color,
    public val highRisk: Color,
    public val extremeRisk: Color,
    public val gasBox: Color,
    public val gasOutline: Color,
    public val gasContent: Color,
    public val groceryBox: Color,
    public val groceryOutline: Color,
    public val groceryContent: Color,
    public val hotelBox: Color,
    public val hotelOutline: Color,
    public val hotelContent: Color,
    public val stormAIOutline: Color,
    public val locationCircle: Color,
) {
    public val alertRadarActiveContent: Color
        get() = alertWarningContent

    public val lowRisk: Color
        get() = alertClearContent
}

private val LightExtendedColors = ExtendedColors(
    isDark = false,
    exitContainer = Color(0xFFFFDAD7),
    exitText = Color(0xFFBA1A1A),
    alertClearContent = Color(0xFF386A42),
    alertClearContentAlternate = Color(0xFF1E4620),
    alertWatchContainer = Color(0xFFFFDF9E),
    alertWatchContent = Color(0xFF7B5A00),
    alertWarningContainer = Color(0xFFFFDAD6),
    alertWarningContent = Color(0xFF93000A),
    alertRadarClearContainer = Color(0xFFE7F7EA),
    alertRadarClearOutline = Color(0xFF3C8B4C),
    alertRadarClearContent = Color(0xFF205C2C),
    alertRadarActiveContainer = Color(0xFFE4EAF6),
    blueBackground = Color(0xFFE4EAF6),
    welcomeNavyStart = Color(0xFF0F131A),
    welcomeNavyEnd = Color(0xFF08203B),
    purpleBackground = Color(0xFFEFE8F6),
    routingLine = Color(0xFF0046D9),
    routingHeader = Color(0xFF296C59),
    greenPrimary = Color(0xFF6C5677),
    purplePrimaryContainer = Color(0xFFDCBCEA),
    purpleSurfaceContainer = Color(0xFFF8F8F8),
    purpleShadow = Color(0xFFD7D1DC),
    searchBarColor = Color(0xFFFFFFFF),
    iconButtonColor = Color(0xFFFFFFFF),
    stormContainer = Color(0xFFFFDAD9),
    stormText = Color(0xFF410006),
    stormTextNested = Color(0xFF900C3F),
    stormContainerNested = Color(0xFFF5EEEE),
    profileIcon = Color(0xFF679F38),
    moderateRisk = Color(0xFF6B6E40),
    highRisk = Color(0xFF7A573F),
    extremeRisk = Color(0xFF6F4B78),
    gasBox = Color(0xFFFDF0E6),
    gasOutline = Color(0xFFD4854A),
    gasContent = Color(0xFF7B420F),
    groceryBox = Color(0xFFE7F7EA),
    groceryOutline = Color(0xFF3C8B4C),
    groceryContent = Color(0xFF205C2C),
    hotelBox = Color(0xFFE5F0FF),
    hotelOutline = Color(0xFF4377D6),
    hotelContent = Color(0xFF1F4F9F),
    stormAIOutline = Color(0xFF623ABF),
    locationCircle = Color(0xFF1B36FF),
)

private val DarkExtendedColors = ExtendedColors(
    isDark = true,
    exitContainer = Color(0xFF761A12),
    exitText = Color(0xFFFFBEB7),
    alertClearContent = Color(0xFF93AD99),
    alertClearContentAlternate = Color(0xFF62A26F),
    alertWatchContainer = Color(0xFF5A4212),
    alertWatchContent = Color(0xFFFFE09A),
    alertWarningContainer = Color(0xFF761A12),
    alertWarningContent = Color(0xFFFFBEB7),
    alertRadarClearContainer = Color(0xFF232624),
    alertRadarClearOutline = Color(0xFF3B8348),
    alertRadarClearContent = Color(0xFF9BDBA7),
    alertRadarActiveContainer = Color(0xFF2F1E1E),
    blueBackground = Color(0xFF252538),
    welcomeNavyStart = Color(0xFF10151D),
    welcomeNavyEnd = Color(0xFF121821),
    purpleBackground = Color(0xFF312538),
    routingLine = Color(0xFF81C3FF),
    routingHeader = Color(0xFF175646),
    greenPrimary = Color(0xFFD8BDE4),
    purplePrimaryContainer = Color(0xFF5A396E),
    purpleSurfaceContainer = Color(0xFF151115),
    purpleShadow = Color(0xFF1D1A20),
    searchBarColor = Color(0xFF131618),
    iconButtonColor = Color(0xFF131618),
    stormContainer = Color(0xFF412121),
    stormText = Color(0xFFFFD0D0),
    stormTextNested = Color(0xFFFFB3B3),
    stormContainerNested = Color(0xFF503737),
    profileIcon = Color(0xFF5D8F33),
    moderateRisk = Color(0xFFACAD93),
    highRisk = Color(0xFFAD9D93),
    extremeRisk = Color(0xFFA993AD),
    gasBox = Color(0xFF3D2A1A),
    gasOutline = Color(0xFFBF7B3A),
    gasContent = Color(0xFFFFB77A),
    groceryBox = Color(0xFF173821),
    groceryOutline = Color(0xFF3B8348),
    groceryContent = Color(0xFF9BDBA7),
    hotelBox = Color(0xFF172C48),
    hotelOutline = Color(0xFF4172BD),
    hotelContent = Color(0xFFA9CAFF),
    stormAIOutline = Color(0xFF4D318F),
    locationCircle = Color(0xFF364EFF),
)

internal fun stormPilotExtendedColors(darkTheme: Boolean): ExtendedColors =
    if (darkTheme) DarkExtendedColors else LightExtendedColors

internal val LocalExtendedColors: ProvidableCompositionLocal<ExtendedColors> =
    staticCompositionLocalOf {
        error("No ExtendedColors provided. Wrap content in StormPilotTheme.")
    }

public val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
