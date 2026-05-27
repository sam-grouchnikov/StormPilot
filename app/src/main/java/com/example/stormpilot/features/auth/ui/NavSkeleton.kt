package com.example.stormpilot.features.auth.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.features.dashboard.ui.RadarPage
import com.example.stormpilot.features.map.ui.MapsPage
import com.example.stormpilot.features.settings.ui.SettingsPage
import com.example.stormpilot.ui.theme.ExtendedColors

sealed class TabDest(val route: String, val title: String, val icon: ImageVector) {
    data object Radar : TabDest("graphs", "Dashboard", Icons.Outlined.Radar)
    data object Nav : TabDest("home", "Home", Icons.Outlined.LocationOn)
    data object Settings : TabDest("terminal", "Settings", Icons.Outlined.Settings)
}

fun Modifier.coloredShadow(
    color: Color,
    borderRadius: Dp = 50.dp,
    blurRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0.dp,
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blurRadius.toPx(),
                    0f,
                    offsetY.toPx(),
                    color.copy(alpha = 0.5f).toArgb()
                )
            }
        }
        canvas.drawRoundRect(
            left = -spread.toPx(),
            top = -spread.toPx(),
            right = size.width + spread.toPx(),
            bottom = size.height + spread.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

@Composable
private fun FloatingNavBar(
    tabs: List<TabDest>,
    currentRoute: String?,
    onTabSelected: (TabDest) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    // Track each item's x offset and width in pixels so the sliding pill knows where to go
    val itemOffsets = remember { mutableStateOf(IntArray(tabs.size)) }
    val itemWidths = remember { mutableStateOf(IntArray(tabs.size)) }

    val animatedPillX by animateIntAsState(
        targetValue = itemOffsets.value.getOrElse(selectedIndex) { 0 },
        animationSpec = tween(durationMillis = 150),
        label = "pillX"
    )
    val animatedPillWidth by animateIntAsState(
        targetValue = itemWidths.value.getOrElse(selectedIndex) { 0 },
        animationSpec = tween(durationMillis = 150),
        label = "pillWidth"
    )

    val colors = ExtendedColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        Layout(
            modifier = Modifier
                .coloredShadow(
                    color = colors.greenShadow,
                    blurRadius = 6.dp,
                    spread = 1.dp
                )
                .clip(CircleShape)
                .background(colors.greenSurfaceContainer)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            content = {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.greenPrimaryContainer)
                )

                // Nav items
                tabs.forEachIndexed { index, tab ->
                    FloatingNavItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        ) { measurables, constraints ->
            val itemMeasurables = measurables.drop(1)
            val pillMeasurable = measurables[0]

            val itemPlaceables = itemMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }

            val totalWidth = itemPlaceables.sumOf { it.width }
            val height = itemPlaceables.maxOf { it.height }

            var xCursor = 0
            val offsets = IntArray(itemPlaceables.size)
            val widths = IntArray(itemPlaceables.size)
            itemPlaceables.forEachIndexed { i, p ->
                offsets[i] = xCursor
                widths[i] = p.width
                xCursor += p.width
            }
            itemOffsets.value = offsets
            itemWidths.value = widths

            val pillPlaceable = pillMeasurable.measure(
                Constraints.fixed(animatedPillWidth, height)
            )

            layout(totalWidth, height) {
                // Place pill at animated position
                pillPlaceable.placeRelative(animatedPillX, 0)
                // Place items on top
                var x = 0
                itemPlaceables.forEach { p ->
                    p.placeRelative(x, 0)
                    x += p.width
                }
            }
        }
    }
}


@Composable
private fun FloatingNavItem(
    tab: TabDest,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedColors()
    val interactionSource = remember { MutableInteractionSource() }
    val selectedColor = colors.greenPrimary
    val unselectedColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(CircleShape)
            // No per-item background — the sliding pill handles it
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp, horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (selected) selectedColor else unselectedColor.copy(alpha = 0.55f),
                modifier = Modifier.padding(end = 4.dp).size(19.dp)
            )
            Text(
                text = tab.title,
                color = if (selected) selectedColor else unselectedColor.copy(alpha = 0.55f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

// NavSkeleton stays exactly the same as yours — omitted for brevity

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavSkeleton() {
    val isMapDestinationSelected = remember { mutableStateOf(false) }

    StormPilotTheme(dynamicColor = false) {
        val navController = rememberNavController()
        val tabs = listOf(TabDest.Radar, TabDest.Nav, TabDest.Settings)

        val mapContentInsets =
            WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal)
                .union(WindowInsets.displayCutout)
        val standardContentInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isMapDestinationSelected.value,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ),
                    exit = fadeOut(animationSpec = tween(180)) +
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(200)
                            )
                ) {
                    FloatingNavBar(
                        tabs = tabs,
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            val contentModifier = when (currentRoute) {
                TabDest.Nav.route ->
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(mapContentInsets)

                TabDest.Radar.route ->
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(mapContentInsets)

                else ->
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(mapContentInsets)
            }

            Box(modifier = contentModifier) {
                NavHost(
                    navController = navController,
                    startDestination = TabDest.Nav.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / 8 },
                            animationSpec = tween(380)
                        ) + fadeIn(animationSpec = tween(260))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 10 },
                            animationSpec = tween(260)
                        ) + fadeOut(animationSpec = tween(180))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 8 },
                            animationSpec = tween(340)
                        ) + fadeIn(animationSpec = tween(220))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / 8 },
                            animationSpec = tween(240)
                        ) + fadeOut(animationSpec = tween(160))
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(TabDest.Nav.route) {
                        MapsPage(onDestinationSelectedStateChanged = { isSelected ->
                            isMapDestinationSelected.value = isSelected
                        })
                    }
                    composable(TabDest.Radar.route) { RadarPage() }
                    composable(TabDest.Settings.route) { SettingsPage() }
                }
            }
        }
    }
}