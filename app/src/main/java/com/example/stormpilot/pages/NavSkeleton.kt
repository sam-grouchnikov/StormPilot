package com.example.stormpilot.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.compose.StormPilotTheme
import com.example.stormpilot.pages.subnav.dashboard.RadarPage
import com.example.stormpilot.pages.subnav.maps.MapsPage


sealed class TabDest(val route: String, val title: String, val icon: ImageVector) {
    data object Radar : TabDest("graphs", "Graphs", Icons.Outlined.Radar)
    data object Nav : TabDest("home", "Home", Icons.Outlined.LocationOn)
    data object Settings : TabDest("terminal", "Terminal", Icons.Outlined.Settings)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavSkeleton() {
    val isDarkMode = remember {mutableStateOf(true)}
    val isMapDestinationSelected = remember { mutableStateOf(false) }
    StormPilotTheme(darkTheme = isDarkMode.value, dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            val navController = rememberNavController()

            val tabs = listOf(
                TabDest.Radar,
                TabDest.Nav,
                TabDest.Settings
            )

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                bottomBar = {
                    val hideBottomBar = currentRoute == TabDest.Nav.route && isMapDestinationSelected.value
                    if (!hideBottomBar) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp, horizontal = 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 24.dp,
                                    bottomStart = 24.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    modifier = Modifier.padding(bottom = 0.dp)
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
                                                indicatorColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = TabDest.Nav.route,
                    modifier = Modifier.padding(innerPadding)
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

@Composable fun SettingsPage() { PageCenter("Settings content") }


@Composable
fun PageCenter(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
