package com.example.stormpilot.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    data object Radar : TabDest("graphs", "Graphs", Icons.Outlined.Radar)
    data object Nav : TabDest("home", "Home", Icons.Outlined.LocationOn)
    data object Settings : TabDest("terminal", "Terminal", Icons.Outlined.Settings)
}

@Composable
fun NavSkeleton() {
    val isDarkMode = remember { mutableStateOf(true) }
    val isMapDestinationSelected = remember { mutableStateOf(false) }

    StormPilotTheme(darkTheme = isDarkMode.value, dynamicColor = false) {
        val navController = rememberNavController()
        val tabs = listOf(TabDest.Radar, TabDest.Nav, TabDest.Settings)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            modifier = Modifier.padding(horizontal = 8.dp)
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
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .only(androidx.compose.foundation.layout.WindowInsetsSides.Horizontal)
                            .union(WindowInsets.displayCutout)
                    )
            ) {
                NavHost(
                    navController = navController,
                    startDestination = TabDest.Nav.route,
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
fun SettingsPage() {
    PageCenter("Settings content")
}

@Composable
fun PageCenter(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
