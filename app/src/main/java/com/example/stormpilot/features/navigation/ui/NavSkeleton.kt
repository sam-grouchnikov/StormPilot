package com.example.stormpilot.features.navigation.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stormpilot.core.StormAiAction
import com.example.stormpilot.features.common.ui.AnimatedStormAiChatBackdrop
import com.example.stormpilot.features.ai.ui.chat.ChatPopup
import com.example.stormpilot.features.dashboard.ui.RadarPage
import com.example.stormpilot.features.map.ui.MapsPage
import com.example.stormpilot.features.navigation.ui.components.FloatingNavBar
import com.example.stormpilot.features.notes.FieldNotesPage
import com.example.stormpilot.features.shared.viewmodels.GenAIViewModel
import com.example.stormpilot.features.settings.ui.SettingsPage
import com.example.stormpilot.features.shared.viewmodels.MapsViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex

sealed class TabDest(val route: String, val title: String, val icon: ImageVector) {
    data object Radar : TabDest("dashboard", "Dashboard", Icons.Outlined.Radar)
    data object Nav : TabDest("navigation", "Navigation", Icons.Outlined.LocationOn)
    data object Notes : TabDest("notes", "Notes", Icons.AutoMirrored.Outlined.StickyNote2)
}

/**
 * Hosts the top-level destinations and the shared floating tab bar, while
 * letting map-specific overlays temporarily take over the full screen.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavSkeleton() {
    val isMapDestinationSelected = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    var showStormAiChat by remember { mutableStateOf(false) }
    var assistantMessage by remember { mutableStateOf<String?>(null) }
    var confirmationMessage by remember { mutableStateOf<String?>(null) }
    var confirmationRiskSummary by remember { mutableStateOf<String?>(null) }
    var confirmationConfirmLabel by remember { mutableStateOf("Confirm") }
    var confirmationCancelLabel by remember { mutableStateOf("Cancel") }
    var pendingConfirmationActions by remember { mutableStateOf<List<StormAiAction>>(emptyList()) }
    val mapsViewModel: MapsViewModel = hiltViewModel()
    val genAIViewModel: GenAIViewModel = hiltViewModel()

    StormPilotTheme(dynamicColor = false) {
        val navController = rememberNavController()
        val tabs = listOf(TabDest.Radar, TabDest.Nav, TabDest.Notes)
        val mapContentInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
        val standardContentInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        fun showConfirmation(
            message: String,
            actions: List<StormAiAction>,
            confirmLabel: String? = null,
            cancelLabel: String? = null,
            riskSummary: String? = null,
        ) {
            confirmationMessage = message
            pendingConfirmationActions = actions
            confirmationConfirmLabel = confirmLabel ?: "Confirm"
            confirmationCancelLabel = cancelLabel ?: "Cancel"
            confirmationRiskSummary = riskSummary
        }

        fun dispatchStormAiActions(actions: List<StormAiAction>) {
            actions.forEach { action ->
                when (action) {
                    is StormAiAction.AnswerOnly -> assistantMessage = action.reply
                    is StormAiAction.AskClarification -> assistantMessage = action.question
                    is StormAiAction.RequestConfirmation -> showConfirmation(
                        message = action.message,
                        actions = action.pendingActions,
                        confirmLabel = action.confirmLabel,
                        cancelLabel = action.cancelLabel,
                        riskSummary = action.riskSummary,
                    )

                    else -> {
                        if (action.requiresConfirmation) {
                            showConfirmation(
                                message = "Apply this StormAI action?",
                                actions = listOf(action),
                            )
                        } else {
                            navController.navigate(TabDest.Nav.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                            mapsViewModel.executeAssistantAction(action)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                val contentModifier = when (currentRoute) {
                    TabDest.Nav.route,
                    TabDest.Radar.route -> Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(mapContentInsets)

                    else -> Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(standardContentInsets)
                }

                Box(modifier = contentModifier) {
                    NavHost(
                        navController = navController,
                        startDestination = TabDest.Nav.route,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it / 8 },
                                animationSpec = tween(380),
                            ) + fadeIn(animationSpec = tween(260))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 10 },
                                animationSpec = tween(260),
                            ) + fadeOut(animationSpec = tween(180))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it / 8 },
                                animationSpec = tween(340),
                            ) + fadeIn(animationSpec = tween(220))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it / 8 },
                                animationSpec = tween(240),
                            ) + fadeOut(animationSpec = tween(160))
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        composable(TabDest.Nav.route) {
                            MapsPage(
                                viewModel = mapsViewModel,
                                onDestinationSelectedStateChanged = { isSelected ->
                                    isMapDestinationSelected.value = isSelected
                                },
                                onOpenSettings = { showSettings.value = true },
                                onOpenStormAiChat = { showStormAiChat = true },
                            )
                        }
                        composable(TabDest.Radar.route) {
                            RadarPage(onOpenSettings = { showSettings.value = true })
                        }
                        composable(TabDest.Notes.route) {
                            FieldNotesPage()
                        }
                    }
                }
            }

            AnimatedStormAiChatBackdrop(
                visible = showStormAiChat,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            )

            StormPilotBottomControls(
                tabs = tabs,
                currentRoute = currentRoute,
                showNavBar = currentRoute != TabDest.Nav.route || !isMapDestinationSelected.value,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            if (showStormAiChat) {
                ChatPopup(
                    onDismiss = { showStormAiChat = false },
                    viewModel = genAIViewModel,
                    onActions = { actions ->
                        dispatchStormAiActions(actions)
                        if (actions.any { it !is StormAiAction.AnswerOnly && it !is StormAiAction.AskClarification }) {
                            showStormAiChat = false
                        }
                    },
                )
            }

            AnimatedVisibility(
                visible = showSettings.value,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(420),
                ) + fadeIn(animationSpec = tween(180)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(320),
                ) + fadeOut(animationSpec = tween(140)),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(4f),
            ) {
                SettingsPage(onDismiss = { showSettings.value = false })
            }

            assistantMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { assistantMessage = null },
                    title = { Text(text = "StormAI") },
                    text = { Text(text = message) },
                    confirmButton = {
                        TextButton(onClick = { assistantMessage = null }) {
                            Text(text = "OK")
                        }
                    },
                )
            }

            confirmationMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = {
                        confirmationMessage = null
                        pendingConfirmationActions = emptyList()
                    },
                    title = { Text(text = "Confirm StormAI action") },
                    text = {
                        Text(
                            text = listOfNotNull(message, confirmationRiskSummary)
                                .joinToString("\n\n"),
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val actions = pendingConfirmationActions
                                confirmationMessage = null
                                pendingConfirmationActions = emptyList()
                                dispatchStormAiActions(actions.map { clearConfirmationRequirement(it) })
                            },
                        ) {
                            Text(text = confirmationConfirmLabel)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                confirmationMessage = null
                                pendingConfirmationActions = emptyList()
                            },
                        ) {
                            Text(text = confirmationCancelLabel)
                        }
                    },
                )
            }
        }
    }
}

private fun clearConfirmationRequirement(action: StormAiAction): StormAiAction =
    when (action) {
        is StormAiAction.AnswerOnly -> action.copy(requiresConfirmation = false)
        is StormAiAction.AskClarification -> action.copy(requiresConfirmation = false)
        is StormAiAction.SetDestination -> action.copy(requiresConfirmation = false)
        is StormAiAction.ShowAlertsOverlay -> action.copy(requiresConfirmation = false)
        is StormAiAction.ShowAlertDetail -> action.copy(requiresConfirmation = false)
        is StormAiAction.PreviewRoute -> action.copy(requiresConfirmation = false)
        is StormAiAction.RequestConfirmation -> action.copy(requiresConfirmation = false)
    }

@Composable
private fun StormPilotBottomControls(
    tabs: List<TabDest>,
    currentRoute: String?,
    showNavBar: Boolean,
    onTabSelected: (TabDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = showNavBar,
        enter = fadeIn(animationSpec = tween(300)) +
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
        exit = fadeOut(animationSpec = tween(180)) +
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200),
            ),
        modifier = modifier,
    ) {
        FloatingNavBar(
            tabs = tabs,
            currentRoute = currentRoute,
            onTabSelected = onTabSelected,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        )
    }
}
