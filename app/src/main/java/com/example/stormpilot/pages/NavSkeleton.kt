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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.compose.StormPilotTheme

sealed class TabDest(val route: String, val title: String, val icon: ImageVector) {
    data object Radar : TabDest("graphs", "Graphs", Icons.Outlined.Radar)
    data object Nav : TabDest("home", "Home", Icons.Outlined.LocationOn)
    data object Settings : TabDest("terminal", "Terminal", Icons.Outlined.Settings)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavSkeleton() {
    val isDarkMode = remember {mutableStateOf(true)}
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
            val currentTab = tabs.firstOrNull { it.route == currentRoute } ?: TabDest.Radar

            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 19.dp, top = 10.dp, end=16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
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

            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = TabDest.Nav.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(TabDest.Nav.route) { NavPage() }
                    composable(TabDest.Radar.route) { RadarPage() }
                    composable(TabDest.Settings.route) { SettingsPage() }
                }
            }
        }
    }
}

@Composable fun NavPage() { PageCenter("Nav content") }
@Composable fun RadarPage() { PageCenter("Radar content") }
@Composable fun SettingsPage() { PageCenter("Settings content") }


@Composable
fun PageCenter(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}