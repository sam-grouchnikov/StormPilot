package com.example.stormpilot.pages

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.subnav.dashboard.RadarPage
import com.example.stormpilot.pages.subnav.maps.MapsPage

sealed class TabDest(val route: String, val title: String, val icon: ImageVector) {
    data object Radar : TabDest("graphs", "Dashboard", Icons.Outlined.Radar)
    data object Nav : TabDest("home", "Home", Icons.Outlined.LocationOn)
    data object Settings : TabDest("terminal", "Settings", Icons.Outlined.Settings)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavSkeleton() {
    val isDarkMode = remember { mutableStateOf(true) }
    val isMapDestinationSelected = remember { mutableStateOf(false) }

    StormPilotTheme(darkTheme = isDarkMode.value, dynamicColor = false) {
        val navController = rememberNavController()
        val tabs = listOf(TabDest.Radar, TabDest.Nav, TabDest.Settings)
        val mapContentInsets =
            WindowInsets.safeDrawing
                .only(androidx.compose.foundation.layout.WindowInsetsSides.Horizontal)
                .union(WindowInsets.displayCutout)
        val standardContentInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isMapDestinationSelected.value,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(100)),
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .height(125.dp)
                            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,

                                    selectedTextColor = MaterialTheme.colorScheme.primary,

                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val contentModifier = if (currentRoute == TabDest.Nav.route) {
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(mapContentInsets)
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(standardContentInsets)
            }

            Box(
                modifier = contentModifier
            ) {
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
                    modifier = Modifier
                        .fillMaxSize()
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



@Composable
fun PageCenter(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
