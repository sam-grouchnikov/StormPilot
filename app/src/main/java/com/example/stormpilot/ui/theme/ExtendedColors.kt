package com.example.stormpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Seed used to generate the Material 3 expressive color scheme.
 */
public val StormPilotColorSeed: Color = Color(0xFF2C8BB0)

@Immutable
public data class ExtendedColors(
    /** Navigation bars, route guidance, and floating controls. */
    public val navigation: ExtendedNavigationColors,
    /** Weather alerts, warning rows, and alert polygons. */
    public val alerts: ExtendedAlertColors,
    /** Weather charts, animated weather icons, risk chips, and SPC outlook badges. */
    public val weather: ExtendedWeatherColors,
    /** Search cards and place category shortcuts. */
    public val search: ExtendedSearchColors,
    /** Map overlays, user location markers, and radar site markers. */
    public val map: ExtendedMapColors,
    /** Onboarding and sign-in/sign-up surfaces. */
    public val auth: ExtendedAuthColors,
    /** StormPilot AI glow, backdrop, and icon colors. */
    public val stormAi: ExtendedStormAiColors,
    /** Account avatar colors. */
    public val profile: ExtendedProfileColors,
)

@Immutable
public data class ExtendedNavigationColors(
    /** Bottom nav container and dashboard navigation bar background. */
    public val barSurface: Color,
    /** Selected pill behind the active bottom nav item. */
    public val selectedPillContainer: Color,
    /** Floating map/search controls and the navigation re-center button. */
    public val floatingControlContainer: Color,
    /** Unselected radar and severe-alert floating map buttons. */
    public val floatingControlButtonContainer: Color,
    /** Drop shadow color for bottom nav and floating map controls. */
    public val floatingControlShadow: Color,
    /** Route polyline drawn on the map. */
    public val routeLine: Color,
    /** Navigation-mode instruction header background. */
    public val instructionContainer: Color,
    /** Icon and text color inside the instruction header. */
    public val instructionContent: Color,
    /** Exit-navigation button background. */
    public val exitButtonContainer: Color,
    /** Exit-navigation button text color. */
    public val exitButtonContent: Color,
)

@Immutable
public data class ExtendedAlertColors(
    /** Clear-alert labels and low-risk alert accents. */
    public val clearContent: Color,
    /** Stronger clear-alert icon accent used in route and alert rows. */
    public val clearEmphasisContent: Color,
    /** Watch alert row background. */
    public val watchContainer: Color,
    /** Watch alert row text and icon color. */
    public val watchContent: Color,
    /** Warning alert row background. */
    public val warningContainer: Color,
    /** Warning alert titles and icons. */
    public val warningContent: Color,
    /** Warning alert supporting text. */
    public val warningSupportingContent: Color,
    /** Tornado warning polygon fill on the main map. */
    public val mapTornadoFill: Color,
    /** Severe thunderstorm warning polygon fill on the main map. */
    public val mapSevereThunderstormFill: Color,
    /** Flash flood warning polygon fill on the main map. */
    public val mapFlashFloodFill: Color,
    /** Border stroke behind main-map alert outlines. */
    public val mapOutlineBorder: Color,
    /** Tornado warning polygon outline on the main map. */
    public val mapTornadoOutline: Color,
    /** Severe thunderstorm warning polygon outline on the main map. */
    public val mapSevereThunderstormOutline: Color,
    /** Flash flood warning polygon outline on the main map. */
    public val mapFlashFloodOutline: Color,
    /** Tornado warning polygon fill in the dashboard alert map card. */
    public val dashboardMapTornadoFill: Color,
    /** Severe thunderstorm warning polygon fill in the dashboard alert map card. */
    public val dashboardMapSevereThunderstormFill: Color,
    /** Flash flood warning polygon fill in the dashboard alert map card. */
    public val dashboardMapFlashFloodFill: Color,
    /** Tornado warning polygon outline in the dashboard alert map card. */
    public val dashboardMapTornadoOutline: Color,
    /** Severe thunderstorm warning polygon outline in the dashboard alert map card. */
    public val dashboardMapSevereThunderstormOutline: Color,
    /** Flash flood warning polygon outline in the dashboard alert map card. */
    public val dashboardMapFlashFloodOutline: Color,
)

@Immutable
public data class ExtendedWeatherColors(
    /** Temperature chart line and selected weather accent. */
    public val temperatureChartLine: Color,
    /** Precipitation chart line. */
    public val precipitationChartLine: Color,
    /** Sunny animated weather icon tint. */
    public val sunIcon: Color,
    /** Rain drop animated weather icon tint. */
    public val rainIcon: Color,
    /** Snowflake animated weather icon tint. */
    public val snowIcon: Color,
    /** Lightning animated weather icon tint. */
    public val lightningIcon: Color,
    /** Low storm-spec risk meter color. */
    public val stormRiskLow: Color,
    /** Moderate storm-spec risk meter color. */
    public val stormRiskModerate: Color,
    /** High storm-spec risk meter color. */
    public val stormRiskHigh: Color,
    /** Extreme storm-spec risk meter color. */
    public val stormRiskExtreme: Color,
    /** General thunderstorm outlook badge background. */
    public val outlookThunderstormContainer: Color,
    /** General thunderstorm outlook badge text. */
    public val outlookThunderstormContent: Color,
    /** Marginal outlook badge background. */
    public val outlookMarginalContainer: Color,
    /** Marginal outlook badge text. */
    public val outlookMarginalContent: Color,
    /** Slight outlook badge background. */
    public val outlookSlightContainer: Color,
    /** Slight outlook badge text. */
    public val outlookSlightContent: Color,
    /** Enhanced outlook badge background. */
    public val outlookEnhancedContainer: Color,
    /** Enhanced outlook badge text. */
    public val outlookEnhancedContent: Color,
    /** Moderate outlook badge background. */
    public val outlookModerateContainer: Color,
    /** Moderate outlook badge text. */
    public val outlookModerateContent: Color,
    /** High outlook badge background. */
    public val outlookHighContainer: Color,
    /** High outlook badge text. */
    public val outlookHighContent: Color,
)

@Immutable
public data class ExtendedSearchColors(
    /** Start color for the StormPilot AI search-card gradient. */
    public val stormAiGradientStart: Color,
    /** End color for the StormPilot AI search-card gradient. */
    public val stormAiGradientEnd: Color,
    /** Border color for the StormPilot AI search card. */
    public val stormAiOutline: Color,
    /** Gas shortcut card background. */
    public val gasContainer: Color,
    /** Gas shortcut card border. */
    public val gasOutline: Color,
    /** Gas shortcut icon and label. */
    public val gasContent: Color,
    /** Grocery shortcut card background. */
    public val groceryContainer: Color,
    /** Grocery shortcut card border. */
    public val groceryOutline: Color,
    /** Grocery shortcut icon and label. */
    public val groceryContent: Color,
    /** Hotel shortcut card background. */
    public val hotelContainer: Color,
    /** Hotel shortcut card border. */
    public val hotelOutline: Color,
    /** Hotel shortcut icon and label. */
    public val hotelContent: Color,
)

@Immutable
public data class ExtendedMapColors(
    /** Dashboard alert map card background. */
    public val locationAlertsCardContainer: Color,
    /** Current user location dot fill. */
    public val userLocationFill: Color,
    /** Current user location shadow. */
    public val userLocationShadow: Color,
    /** Current user location dot stroke. */
    public val userLocationStroke: Color,
    /** Default radar site marker fill. */
    public val radarSiteFill: Color,
    /** Default radar site marker stroke. */
    public val radarSiteStroke: Color,
    /** Selected radar site marker fill. */
    public val selectedRadarSiteFill: Color,
    /** Selected radar site marker stroke. */
    public val selectedRadarSiteStroke: Color,
)

@Immutable
public data class ExtendedAuthColors(
    /** Onboarding and auth gradient start color. */
    public val backgroundStart: Color,
    /** Onboarding and auth gradient end color. */
    public val backgroundEnd: Color,
    /** Main text, outline button text, and auth field input text. */
    public val primaryContent: Color,
    /** Supporting onboarding/auth copy. */
    public val supportingContent: Color,
    /** Existing-account/new-account prompt text. */
    public val promptContent: Color,
    /** Auth primary button background. */
    public val primaryButtonContainer: Color,
    /** Auth outlined button border. */
    public val outlineButtonBorder: Color,
    /** Focused auth input background. */
    public val focusedInputContainer: Color,
    /** Unfocused auth input background. */
    public val unfocusedInputContainer: Color,
    /** Focused auth input border. */
    public val focusedInputBorder: Color,
    /** Unfocused auth input border. */
    public val unfocusedInputBorder: Color,
    /** Focused auth field label and icon. */
    public val focusedInputContent: Color,
    /** Unfocused auth field label. */
    public val unfocusedInputLabel: Color,
    /** Focused auth input icon bubble. */
    public val focusedInputIconContainer: Color,
    /** Unfocused auth input icon bubble. */
    public val unfocusedInputIconContainer: Color,
    /** Unfocused auth input icon. */
    public val unfocusedInputIcon: Color,
    /** Placeholder text inside auth input fields. */
    public val inputPlaceholder: Color,
)

@Immutable
public data class ExtendedStormAiColors(
    /** Blue stop in StormPilot AI glow gradients. */
    public val glowBlue: Color,
    /** Purple stop in StormPilot AI glow gradients. */
    public val glowPurple: Color,
    /** Magenta stop in StormPilot AI glow gradients. */
    public val glowMagenta: Color,
    /** White core used for StormPilot AI icons and glow strokes. */
    public val iconContent: Color,
    /** Dark scrim color behind the animated chat backdrop. */
    public val backdropScrim: Color,
) {
    public fun glowColors(): List<Color> =
        listOf(glowBlue, glowPurple, glowMagenta)

    public fun glowColorInts(): IntArray =
        intArrayOf(glowBlue.toArgb(), glowPurple.toArgb(), glowMagenta.toArgb(), glowBlue.toArgb())
}

@Immutable
public data class ExtendedProfileColors(
    /** Account avatar circle background. */
    public val avatarContainer: Color,
    /** Account avatar initial text. */
    public val avatarContent: Color,
)

private val LightExtendedColors = ExtendedColors(
    navigation = ExtendedNavigationColors(
        barSurface = Color(0xFFF8F8F8),
        selectedPillContainer = Color(0xFFDCBCEA),
        floatingControlContainer = Color(0xFFFFFFFF),
        floatingControlButtonContainer = Color(0xFFFFFFFF),
        floatingControlShadow = Color(0xFFD7D1DC),
        routeLine = Color(0xFF0046D9),
        instructionContainer = Color(0xFF296C59),
        instructionContent = Color(0xFFFFFFFF),
        exitButtonContainer = Color(0xFFFFDAD7),
        exitButtonContent = Color(0xFFBA1A1A),
    ),
    alerts = ExtendedAlertColors(
        clearContent = Color(0xFF386A42),
        clearEmphasisContent = Color(0xFF1E4620),
        watchContainer = Color(0xFFFFDF9E),
        watchContent = Color(0xFF7B5A00),
        warningContainer = Color(0xFFFFDAD6),
        warningContent = Color(0xFF93000A),
        warningSupportingContent = Color(0xFF792A2A),
        mapTornadoFill = Color(0x1AFF0000),
        mapSevereThunderstormFill = Color(0x1AFF9F15),
        mapFlashFloodFill = Color(0x1A00BB00),
        mapOutlineBorder = Color(0xFF000000),
        mapTornadoOutline = Color(0x80FF0000),
        mapSevereThunderstormOutline = Color(0xFFD26D03),
        mapFlashFloodOutline = Color(0x8000FF00),
        dashboardMapTornadoFill = Color(0x22FF0000),
        dashboardMapSevereThunderstormFill = Color(0x22FFD700),
        dashboardMapFlashFloodFill = Color(0x2200BB00),
        dashboardMapTornadoOutline = Color(0xC8FF3030),
        dashboardMapSevereThunderstormOutline = Color(0xFFFFB020),
        dashboardMapFlashFloodOutline = Color(0xC800E676),
    ),
    weather = ExtendedWeatherColors(
        temperatureChartLine = Color(0xFF7C4FD9),
        precipitationChartLine = Color(0xFF087EA4),
        sunIcon = Color(0xFFFFB300),
        rainIcon = Color(0xFF64B5F6),
        snowIcon = Color(0xFF90CAF9),
        lightningIcon = Color(0xFFFFC107),
        stormRiskLow = Color(0xFF386A42),
        stormRiskModerate = Color(0xFFB9A23C),
        stormRiskHigh = Color(0xFFEA5353),
        stormRiskExtreme = Color(0xFFE36DD8),
        outlookThunderstormContainer = Color(0xFF85DCD5),
        outlookThunderstormContent = Color(0xFF0F4C45),
        outlookMarginalContainer = Color(0xFF8DDC85),
        outlookMarginalContent = Color(0xFF2F4C2C),
        outlookSlightContainer = Color(0xFFF2DE84),
        outlookSlightContent = Color(0xFF5A4C21),
        outlookEnhancedContainer = Color(0xFFFFBD9A),
        outlookEnhancedContent = Color(0xFF612E0C),
        outlookModerateContainer = Color(0xFFFFB7B7),
        outlookModerateContent = Color(0xFF792A2A),
        outlookHighContainer = Color(0xFFF2C4FF),
        outlookHighContent = Color(0xFF76306A),
    ),
    search = ExtendedSearchColors(
        stormAiGradientStart = Color(0xFF7173FF),
        stormAiGradientEnd = Color(0xFF9D2CFF),
        stormAiOutline = Color(0xFF623ABF),
        gasContainer = Color(0xFFFDF0E6),
        gasOutline = Color(0xFFD4854A),
        gasContent = Color(0xFF7B420F),
        groceryContainer = Color(0xFFE7F7EA),
        groceryOutline = Color(0xFF3C8B4C),
        groceryContent = Color(0xFF205C2C),
        hotelContainer = Color(0xFFE5F0FF),
        hotelOutline = Color(0xFF4377D6),
        hotelContent = Color(0xFF1F4F9F),
    ),
    map = ExtendedMapColors(
        locationAlertsCardContainer = Color(0xFFE4EAF6),
        userLocationFill = Color(0xFF1B36FF),
        userLocationShadow = Color(0xA6000000),
        userLocationStroke = Color(0xFFFFFFFF),
        radarSiteFill = Color(0xFFCCCCCC),
        radarSiteStroke = Color(0xFF868686),
        selectedRadarSiteFill = Color(0xFF54AD44),
        selectedRadarSiteStroke = Color(0xFF606060),
    ),
    auth = ExtendedAuthColors(
        backgroundStart = Color(0xFF0F131A),
        backgroundEnd = Color(0xFF08203B),
        primaryContent = Color(0xFFFFFFFF),
        supportingContent = Color(0xBDFFFFFF),
        promptContent = Color(0xADFFFFFF),
        primaryButtonContainer = Color(0xFFFFFFFF),
        outlineButtonBorder = Color(0x70FFFFFF),
        focusedInputContainer = Color(0x2EFFFFFF),
        unfocusedInputContainer = Color(0x1AFFFFFF),
        focusedInputBorder = Color(0xC2FFFFFF),
        unfocusedInputBorder = Color(0x3DFFFFFF),
        focusedInputContent = Color(0xFFFFFFFF),
        unfocusedInputLabel = Color(0xBDFFFFFF),
        focusedInputIconContainer = Color(0x38FFFFFF),
        unfocusedInputIconContainer = Color(0x1FFFFFFF),
        unfocusedInputIcon = Color(0xB8FFFFFF),
        inputPlaceholder = Color(0x75FFFFFF),
    ),
    stormAi = ExtendedStormAiColors(
        glowBlue = Color(0xFF4A90E2),
        glowPurple = Color(0xFF7B4FD6),
        glowMagenta = Color(0xFFE040C8),
        iconContent = Color(0xFFFFFFFF),
        backdropScrim = Color(0xFF000000),
    ),
    profile = ExtendedProfileColors(
        avatarContainer = Color(0xFF679F38),
        avatarContent = Color(0xFFFFFFFF),
    ),
)

private val DarkExtendedColors = ExtendedColors(
    navigation = ExtendedNavigationColors(
        barSurface = Color(0xFF151115),
        selectedPillContainer = Color(0xFF5A396E),
        floatingControlContainer = Color(0xFF131618),
        floatingControlButtonContainer = Color(0xFF131618),
        floatingControlShadow = Color(0xFF1D1A20),
        routeLine = Color(0xFF81C3FF),
        instructionContainer = Color(0xFF175646),
        instructionContent = Color(0xFFFFFFFF),
        exitButtonContainer = Color(0xFF761A12),
        exitButtonContent = Color(0xFFFFBEB7),
    ),
    alerts = ExtendedAlertColors(
        clearContent = Color(0xFF93AD99),
        clearEmphasisContent = Color(0xFF62A26F),
        watchContainer = Color(0xFF5A4212),
        watchContent = Color(0xFFFFE09A),
        warningContainer = Color(0xFF761A12),
        warningContent = Color(0xFFFFBEB7),
        warningSupportingContent = Color(0xFFFFC2C2),
        mapTornadoFill = Color(0x1AFF0000),
        mapSevereThunderstormFill = Color(0x1AFF9F15),
        mapFlashFloodFill = Color(0x1A00BB00),
        mapOutlineBorder = Color(0xFF000000),
        mapTornadoOutline = Color(0x80FF0000),
        mapSevereThunderstormOutline = Color(0xFFD26D03),
        mapFlashFloodOutline = Color(0x8000FF00),
        dashboardMapTornadoFill = Color(0x22FF0000),
        dashboardMapSevereThunderstormFill = Color(0x22FFD700),
        dashboardMapFlashFloodFill = Color(0x2200BB00),
        dashboardMapTornadoOutline = Color(0xC8FF3030),
        dashboardMapSevereThunderstormOutline = Color(0xFFFFB020),
        dashboardMapFlashFloodOutline = Color(0xC800E676),
    ),
    weather = ExtendedWeatherColors(
        temperatureChartLine = Color(0xFFB291F3),
        precipitationChartLine = Color(0xFF7DDCFF),
        sunIcon = Color(0xFFFFB300),
        rainIcon = Color(0xFF64B5F6),
        snowIcon = Color(0xFF90CAF9),
        lightningIcon = Color(0xFFFFC107),
        stormRiskLow = Color(0xFF93AD99),
        stormRiskModerate = Color(0xFFD3AC65),
        stormRiskHigh = Color(0xFFC57267),
        stormRiskExtreme = Color(0xFFBE83C0),
        outlookThunderstormContainer = Color(0xFF0F4C45),
        outlookThunderstormContent = Color(0xFF85DCD5),
        outlookMarginalContainer = Color(0xFF2F4C2C),
        outlookMarginalContent = Color(0xFF88CC81),
        outlookSlightContainer = Color(0xFF5A4C21),
        outlookSlightContent = Color(0xFFDDD286),
        outlookEnhancedContainer = Color(0xFF612E0C),
        outlookEnhancedContent = Color(0xFFFFBD9A),
        outlookModerateContainer = Color(0xFF792A2A),
        outlookModerateContent = Color(0xFFFD8888),
        outlookHighContainer = Color(0xFF76306A),
        outlookHighContent = Color(0xFFE6A5FC),
    ),
    search = ExtendedSearchColors(
        stormAiGradientStart = Color(0xFF7173FF),
        stormAiGradientEnd = Color(0xFF9D2CFF),
        stormAiOutline = Color(0xFF4D318F),
        gasContainer = Color(0xFF3D2A1A),
        gasOutline = Color(0xFFBF7B3A),
        gasContent = Color(0xFFFFB77A),
        groceryContainer = Color(0xFF173821),
        groceryOutline = Color(0xFF3B8348),
        groceryContent = Color(0xFF9BDBA7),
        hotelContainer = Color(0xFF172C48),
        hotelOutline = Color(0xFF4172BD),
        hotelContent = Color(0xFFA9CAFF),
    ),
    map = ExtendedMapColors(
        locationAlertsCardContainer = Color(0xFF252538),
        userLocationFill = Color(0xFF364EFF),
        userLocationShadow = Color(0xA6000000),
        userLocationStroke = Color(0xFFFFFFFF),
        radarSiteFill = Color(0xFFCCCCCC),
        radarSiteStroke = Color(0xFF868686),
        selectedRadarSiteFill = Color(0xFF54AD44),
        selectedRadarSiteStroke = Color(0xFF606060),
    ),
    auth = ExtendedAuthColors(
        backgroundStart = Color(0xFF10151D),
        backgroundEnd = Color(0xFF121821),
        primaryContent = Color(0xFFFFFFFF),
        supportingContent = Color(0xBDFFFFFF),
        promptContent = Color(0xADFFFFFF),
        primaryButtonContainer = Color(0xFFFFFFFF),
        outlineButtonBorder = Color(0x70FFFFFF),
        focusedInputContainer = Color(0x2EFFFFFF),
        unfocusedInputContainer = Color(0x1AFFFFFF),
        focusedInputBorder = Color(0xC2FFFFFF),
        unfocusedInputBorder = Color(0x3DFFFFFF),
        focusedInputContent = Color(0xFFFFFFFF),
        unfocusedInputLabel = Color(0xBDFFFFFF),
        focusedInputIconContainer = Color(0x38FFFFFF),
        unfocusedInputIconContainer = Color(0x1FFFFFFF),
        unfocusedInputIcon = Color(0xB8FFFFFF),
        inputPlaceholder = Color(0x75FFFFFF),
    ),
    stormAi = ExtendedStormAiColors(
        glowBlue = Color(0xFF4A90E2),
        glowPurple = Color(0xFF7B4FD6),
        glowMagenta = Color(0xFFE040C8),
        iconContent = Color(0xFFFFFFFF),
        backdropScrim = Color(0xFF000000),
    ),
    profile = ExtendedProfileColors(
        avatarContainer = Color(0xFF5D8F33),
        avatarContent = Color(0xFFFFFFFF),
    ),
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
