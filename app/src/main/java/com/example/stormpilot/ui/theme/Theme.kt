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

/**
 * Change this seed to recolor the whole app with the Material 3 expressive palette.
 */
val StormPilotColorSeed = Color(0xFF2C8BB0)

@SuppressLint("RestrictedApi")
private val expressiveColors = MaterialDynamicColors()

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
            primary = expressiveColors.primary().composeColor(scheme),
            onPrimary = expressiveColors.onPrimary().composeColor(scheme),
            primaryContainer = expressiveColors.primaryContainer().composeColor(scheme),
            onPrimaryContainer = expressiveColors.onPrimaryContainer().composeColor(scheme),
            inversePrimary = expressiveColors.inversePrimary().composeColor(scheme),
            secondary = expressiveColors.secondary().composeColor(scheme),
            onSecondary = expressiveColors.onSecondary().composeColor(scheme),
            secondaryContainer = expressiveColors.secondaryContainer().composeColor(scheme),
            onSecondaryContainer = expressiveColors.onSecondaryContainer().composeColor(scheme),
            tertiary = expressiveColors.tertiary().composeColor(scheme),
            onTertiary = expressiveColors.onTertiary().composeColor(scheme),
            tertiaryContainer = expressiveColors.tertiaryContainer().composeColor(scheme),
            onTertiaryContainer = expressiveColors.onTertiaryContainer().composeColor(scheme),
            background = expressiveColors.background().composeColor(scheme),
            onBackground = expressiveColors.onBackground().composeColor(scheme),
            surface = expressiveColors.surface().composeColor(scheme),
            onSurface = expressiveColors.onSurface().composeColor(scheme),
            surfaceVariant = expressiveColors.surfaceVariant().composeColor(scheme),
            onSurfaceVariant = expressiveColors.onSurfaceVariant().composeColor(scheme),
            surfaceTint = expressiveColors.surfaceTint().composeColor(scheme),
            inverseSurface = expressiveColors.inverseSurface().composeColor(scheme),
            inverseOnSurface = expressiveColors.inverseOnSurface().composeColor(scheme),
            error = expressiveColors.error().composeColor(scheme),
            onError = expressiveColors.onError().composeColor(scheme),
            errorContainer = expressiveColors.errorContainer().composeColor(scheme),
            onErrorContainer = expressiveColors.onErrorContainer().composeColor(scheme),
            outline = expressiveColors.outline().composeColor(scheme),
            outlineVariant = expressiveColors.outlineVariant().composeColor(scheme),
            scrim = expressiveColors.scrim().composeColor(scheme),
            surfaceBright = expressiveColors.surfaceBright().composeColor(scheme),
            surfaceContainer = expressiveColors.surfaceContainer().composeColor(scheme),
            surfaceContainerHigh = expressiveColors.surfaceContainerHigh().composeColor(scheme),
            surfaceContainerHighest = expressiveColors.surfaceContainerHighest().composeColor(scheme),
            surfaceContainerLow = expressiveColors.surfaceContainerLow().composeColor(scheme),
            surfaceContainerLowest = expressiveColors.surfaceContainerLowest().composeColor(scheme),
            surfaceDim = expressiveColors.surfaceDim().composeColor(scheme),
            primaryFixed = expressiveColors.primaryFixed().composeColor(scheme),
            primaryFixedDim = expressiveColors.primaryFixedDim().composeColor(scheme),
            onPrimaryFixed = expressiveColors.onPrimaryFixed().composeColor(scheme),
            onPrimaryFixedVariant = expressiveColors.onPrimaryFixedVariant().composeColor(scheme),
            secondaryFixed = expressiveColors.secondaryFixed().composeColor(scheme),
            secondaryFixedDim = expressiveColors.secondaryFixedDim().composeColor(scheme),
            onSecondaryFixed = expressiveColors.onSecondaryFixed().composeColor(scheme),
            onSecondaryFixedVariant = expressiveColors.onSecondaryFixedVariant().composeColor(scheme),
            tertiaryFixed = expressiveColors.tertiaryFixed().composeColor(scheme),
            tertiaryFixedDim = expressiveColors.tertiaryFixedDim().composeColor(scheme),
            onTertiaryFixed = expressiveColors.onTertiaryFixed().composeColor(scheme),
            onTertiaryFixedVariant = expressiveColors.onTertiaryFixedVariant().composeColor(scheme),
        )
    } else {
        lightColorScheme(
            primary = expressiveColors.primary().composeColor(scheme),
            onPrimary = expressiveColors.onPrimary().composeColor(scheme),
            primaryContainer = expressiveColors.primaryContainer().composeColor(scheme),
            onPrimaryContainer = expressiveColors.onPrimaryContainer().composeColor(scheme),
            inversePrimary = expressiveColors.inversePrimary().composeColor(scheme),
            secondary = expressiveColors.secondary().composeColor(scheme),
            onSecondary = expressiveColors.onSecondary().composeColor(scheme),
            secondaryContainer = expressiveColors.secondaryContainer().composeColor(scheme),
            onSecondaryContainer = expressiveColors.onSecondaryContainer().composeColor(scheme),
            tertiary = expressiveColors.tertiary().composeColor(scheme),
            onTertiary = expressiveColors.onTertiary().composeColor(scheme),
            tertiaryContainer = expressiveColors.tertiaryContainer().composeColor(scheme),
            onTertiaryContainer = expressiveColors.onTertiaryContainer().composeColor(scheme),
            background = expressiveColors.background().composeColor(scheme),
            onBackground = expressiveColors.onBackground().composeColor(scheme),
            surface = expressiveColors.surface().composeColor(scheme),
            onSurface = expressiveColors.onSurface().composeColor(scheme),
            surfaceVariant = expressiveColors.surfaceVariant().composeColor(scheme),
            onSurfaceVariant = expressiveColors.onSurfaceVariant().composeColor(scheme),
            surfaceTint = expressiveColors.surfaceTint().composeColor(scheme),
            inverseSurface = expressiveColors.inverseSurface().composeColor(scheme),
            inverseOnSurface = expressiveColors.inverseOnSurface().composeColor(scheme),
            error = expressiveColors.error().composeColor(scheme),
            onError = expressiveColors.onError().composeColor(scheme),
            errorContainer = expressiveColors.errorContainer().composeColor(scheme),
            onErrorContainer = expressiveColors.onErrorContainer().composeColor(scheme),
            outline = expressiveColors.outline().composeColor(scheme),
            outlineVariant = expressiveColors.outlineVariant().composeColor(scheme),
            scrim = expressiveColors.scrim().composeColor(scheme),
            surfaceBright = expressiveColors.surfaceBright().composeColor(scheme),
            surfaceContainer = expressiveColors.surfaceContainer().composeColor(scheme),
            surfaceContainerHigh = expressiveColors.surfaceContainerLow().composeColor(scheme),
            surfaceContainerHighest = expressiveColors.surfaceContainerLowest().composeColor(scheme),
            surfaceContainerLow = expressiveColors.surfaceContainerHigh().composeColor(scheme),
            surfaceContainerLowest = expressiveColors.surfaceContainerHighest().composeColor(scheme),
            surfaceDim = expressiveColors.surfaceDim().composeColor(scheme),
            primaryFixed = expressiveColors.primaryFixed().composeColor(scheme),
            primaryFixedDim = expressiveColors.primaryFixedDim().composeColor(scheme),
            onPrimaryFixed = expressiveColors.onPrimaryFixed().composeColor(scheme),
            onPrimaryFixedVariant = expressiveColors.onPrimaryFixedVariant().composeColor(scheme),
            secondaryFixed = expressiveColors.secondaryFixed().composeColor(scheme),
            secondaryFixedDim = expressiveColors.secondaryFixedDim().composeColor(scheme),
            onSecondaryFixed = expressiveColors.onSecondaryFixed().composeColor(scheme),
            onSecondaryFixedVariant = expressiveColors.onSecondaryFixedVariant().composeColor(scheme),
            tertiaryFixed = expressiveColors.tertiaryFixed().composeColor(scheme),
            tertiaryFixedDim = expressiveColors.tertiaryFixedDim().composeColor(scheme),
            onTertiaryFixed = expressiveColors.onTertiaryFixed().composeColor(scheme),
            onTertiaryFixedVariant = expressiveColors.onTertiaryFixedVariant().composeColor(scheme),
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

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = StormPilotShapes,
        typography = StormPilotTypography,
        content = content
    )
}
