package com.example.stormpilot.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.core.isAppInDarkMode
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

@SuppressLint("RestrictedApi")
private val materialColors = MaterialDynamicColors()

private val StormPilotShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@SuppressLint("RestrictedApi")
internal fun stormPilotColorScheme(darkTheme: Boolean): ColorScheme {
    val scheme = SchemeTonalSpot(
        Hct.fromInt(StormPilotColorSeed.toArgb()),
        darkTheme,
        0.0,
    )

    return if (darkTheme) {
        darkColorScheme(
            primary = materialColors.primary().composeColor(scheme),
            onPrimary = materialColors.onPrimary().composeColor(scheme),
            primaryContainer = materialColors.primaryContainer().composeColor(scheme),
            onPrimaryContainer = materialColors.onPrimaryContainer().composeColor(scheme),
            inversePrimary = materialColors.inversePrimary().composeColor(scheme),
            secondary = materialColors.secondary().composeColor(scheme),
            onSecondary = materialColors.onSecondary().composeColor(scheme),
            secondaryContainer = materialColors.secondaryContainer().composeColor(scheme),
            onSecondaryContainer = materialColors.onSecondaryContainer().composeColor(scheme),
            tertiary = materialColors.tertiary().composeColor(scheme),
            onTertiary = materialColors.onTertiary().composeColor(scheme),
            tertiaryContainer = materialColors.tertiaryContainer().composeColor(scheme),
            onTertiaryContainer = materialColors.onTertiaryContainer().composeColor(scheme),
            background = materialColors.background().composeColor(scheme),
            onBackground = materialColors.onBackground().composeColor(scheme),
            surface = materialColors.surface().composeColor(scheme),
            onSurface = materialColors.onSurface().composeColor(scheme),
            surfaceVariant = materialColors.surfaceVariant().composeColor(scheme),
            onSurfaceVariant = materialColors.onSurfaceVariant().composeColor(scheme),
            surfaceTint = materialColors.surfaceTint().composeColor(scheme),
            inverseSurface = materialColors.inverseSurface().composeColor(scheme),
            inverseOnSurface = materialColors.inverseOnSurface().composeColor(scheme),
            error = materialColors.error().composeColor(scheme),
            onError = materialColors.onError().composeColor(scheme),
            errorContainer = materialColors.errorContainer().composeColor(scheme),
            onErrorContainer = materialColors.onErrorContainer().composeColor(scheme),
            outline = materialColors.outline().composeColor(scheme),
            outlineVariant = materialColors.outlineVariant().composeColor(scheme),
            scrim = materialColors.scrim().composeColor(scheme),
            surfaceBright = materialColors.surfaceBright().composeColor(scheme),
            surfaceContainer = materialColors.surfaceContainer().composeColor(scheme),
            surfaceContainerHigh = materialColors.surfaceContainerHigh().composeColor(scheme),
            surfaceContainerHighest = materialColors.surfaceContainerHighest().composeColor(scheme),
            surfaceContainerLow = materialColors.surfaceContainerLow().composeColor(scheme),
            surfaceContainerLowest = materialColors.surfaceContainerLowest().composeColor(scheme),
            surfaceDim = materialColors.surfaceDim().composeColor(scheme),
            primaryFixed = materialColors.primaryFixed().composeColor(scheme),
            primaryFixedDim = materialColors.primaryFixedDim().composeColor(scheme),
            onPrimaryFixed = materialColors.onPrimaryFixed().composeColor(scheme),
            onPrimaryFixedVariant = materialColors.onPrimaryFixedVariant().composeColor(scheme),
            secondaryFixed = materialColors.secondaryFixed().composeColor(scheme),
            secondaryFixedDim = materialColors.secondaryFixedDim().composeColor(scheme),
            onSecondaryFixed = materialColors.onSecondaryFixed().composeColor(scheme),
            onSecondaryFixedVariant = materialColors.onSecondaryFixedVariant().composeColor(scheme),
            tertiaryFixed = materialColors.tertiaryFixed().composeColor(scheme),
            tertiaryFixedDim = materialColors.tertiaryFixedDim().composeColor(scheme),
            onTertiaryFixed = materialColors.onTertiaryFixed().composeColor(scheme),
            onTertiaryFixedVariant = materialColors.onTertiaryFixedVariant().composeColor(scheme),
        )
    } else {
        lightColorScheme(
            primary = materialColors.primary().composeColor(scheme),
            onPrimary = materialColors.onPrimary().composeColor(scheme),
            primaryContainer = materialColors.primaryContainer().composeColor(scheme),
            onPrimaryContainer = materialColors.onPrimaryContainer().composeColor(scheme),
            inversePrimary = materialColors.inversePrimary().composeColor(scheme),
            secondary = materialColors.secondary().composeColor(scheme),
            onSecondary = materialColors.onSecondary().composeColor(scheme),
            secondaryContainer = materialColors.secondaryContainer().composeColor(scheme),
            onSecondaryContainer = materialColors.onSecondaryContainer().composeColor(scheme),
            tertiary = materialColors.tertiary().composeColor(scheme),
            onTertiary = materialColors.onTertiary().composeColor(scheme),
            tertiaryContainer = materialColors.tertiaryContainer().composeColor(scheme),
            onTertiaryContainer = materialColors.onTertiaryContainer().composeColor(scheme),
            background = materialColors.background().composeColor(scheme),
            onBackground = materialColors.onBackground().composeColor(scheme),
            surface = materialColors.surface().composeColor(scheme),
            onSurface = materialColors.onSurface().composeColor(scheme),
            surfaceVariant = materialColors.surfaceVariant().composeColor(scheme),
            onSurfaceVariant = materialColors.onSurfaceVariant().composeColor(scheme),
            surfaceTint = materialColors.surfaceTint().composeColor(scheme),
            inverseSurface = materialColors.inverseSurface().composeColor(scheme),
            inverseOnSurface = materialColors.inverseOnSurface().composeColor(scheme),
            error = materialColors.error().composeColor(scheme),
            onError = materialColors.onError().composeColor(scheme),
            errorContainer = materialColors.errorContainer().composeColor(scheme),
            onErrorContainer = materialColors.onErrorContainer().composeColor(scheme),
            outline = materialColors.outline().composeColor(scheme),
            outlineVariant = materialColors.outlineVariant().composeColor(scheme),
            scrim = materialColors.scrim().composeColor(scheme),
            surfaceBright = materialColors.surfaceBright().composeColor(scheme),
            surfaceContainer = materialColors.surfaceContainer().composeColor(scheme),
            surfaceContainerHigh = materialColors.surfaceContainerHigh().composeColor(scheme),
            surfaceContainerHighest = materialColors.surfaceContainerHighest().composeColor(scheme),
            surfaceContainerLow = materialColors.surfaceContainerLow().composeColor(scheme),
            surfaceContainerLowest = materialColors.surfaceContainerLowest().composeColor(scheme),
            surfaceDim = materialColors.surfaceDim().composeColor(scheme),
            primaryFixed = materialColors.primaryFixed().composeColor(scheme),
            primaryFixedDim = materialColors.primaryFixedDim().composeColor(scheme),
            onPrimaryFixed = materialColors.onPrimaryFixed().composeColor(scheme),
            onPrimaryFixedVariant = materialColors.onPrimaryFixedVariant().composeColor(scheme),
            secondaryFixed = materialColors.secondaryFixed().composeColor(scheme),
            secondaryFixedDim = materialColors.secondaryFixedDim().composeColor(scheme),
            onSecondaryFixed = materialColors.onSecondaryFixed().composeColor(scheme),
            onSecondaryFixedVariant = materialColors.onSecondaryFixedVariant().composeColor(scheme),
            tertiaryFixed = materialColors.tertiaryFixed().composeColor(scheme),
            tertiaryFixedDim = materialColors.tertiaryFixedDim().composeColor(scheme),
            onTertiaryFixed = materialColors.onTertiaryFixed().composeColor(scheme),
            onTertiaryFixedVariant = materialColors.onTertiaryFixedVariant().composeColor(scheme),
        )
    }
}

@SuppressLint("RestrictedApi")
private fun DynamicColor.composeColor(@SuppressLint("RestrictedApi") scheme: SchemeTonalSpot): Color = Color(getArgb(scheme))

@Composable
fun StormPilotTheme(
    darkTheme: Boolean = isAppInDarkMode(),
    dynamicColor: Boolean = true,
    isLoading: Boolean = false,
    opaqueNavigationBar: Boolean = false,
    useSurfaceContainerNavigationBar: Boolean = false,
    navigationBarColorOverride: Color? = null,
    syncAppDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    if (syncAppDarkMode) {
        AppSettings.isDarkMode = darkTheme
    }
    val colorScheme = stormPilotColorScheme(darkTheme)
    val extendedColors = stormPilotExtendedColors(darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            val useDarkIcons = !darkTheme
            val navigationBarSurfaceColor = if (useSurfaceContainerNavigationBar) {
                colorScheme.surfaceContainer
            } else {
                colorScheme.surfaceContainerHigh
            }
            val navigationBarColor = if (opaqueNavigationBar) {
                (navigationBarColorOverride ?: navigationBarSurfaceColor).toArgb()
            } else {
                android.graphics.Color.TRANSPARENT
            }

            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = navigationBarColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            controller.isAppearanceLightStatusBars = useDarkIcons
            controller.isAppearanceLightNavigationBars = useDarkIcons
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = StormPilotShapes,
            typography = StormPilotTypography,
            content = content,
        )
    }
}
