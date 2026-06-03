package com.example.stormpilot.features.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.data.formatDistance
import com.example.stormpilot.data.formatDuration
import com.example.stormpilot.data.MapsUiState
import com.example.stormpilot.data.PhotonFeature
import kotlinx.coroutines.delay
import com.example.stormpilot.features.common.ui.ProfileAvatar
import com.example.stormpilot.ui.theme.ExtendedColors
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScaffold(
    modifier: Modifier = Modifier,
    query: String,
    active: Boolean,
    searchResults: List<PhotonFeature>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSearchSubmit: () -> Unit,
    onResultClick: (PhotonFeature) -> Unit,
    onOpenSettings: () -> Unit = {},
    containerColor: Color,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val statusBarTopPadding = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val surfaceTopPadding by animateDpAsState(
        targetValue = if (active) 0.dp else statusBarTopPadding + 6.dp,
        animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "maps_search_top_padding",
    )
    val surfaceHorizontalPadding by animateDpAsState(
        targetValue = if (active) 0.dp else 8.dp,
        animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "maps_search_horizontal_padding",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (active) 0.dp else 30.dp,
        animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "maps_search_corner_radius",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (active) 0.dp else 8.dp,
        animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
        label = "maps_search_elevation",
    )

    LaunchedEffect(active) {
        if (active) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val surfaceHeight by animateDpAsState(
            targetValue = if (active) maxHeight else 60.dp,
            animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
            label = "maps_search_height",
        )

        Surface(
            modifier = Modifier
                .padding(
                    start = surfaceHorizontalPadding,
                    end = surfaceHorizontalPadding,
                    top = surfaceTopPadding,
                )
                .fillMaxWidth()
                .height(surfaceHeight),
            shape = RoundedCornerShape(cornerRadius),
            color = containerColor,
            shadowElevation = shadowElevation,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = active,
                    enter = expandVertically(animationSpec = tween(240, easing = FastOutSlowInEasing)),
                    exit = shrinkVertically(animationSpec = tween(160, easing = FastOutSlowInEasing)),
                ) {
                    Spacer(modifier = Modifier.height(statusBarTopPadding))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(enabled = !active) { onActiveChange(true) }
                        .padding(start = 4.dp, end = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            if (active) {
                                onActiveChange(false)
                            } else {
                                onActiveChange(true)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (active) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Search,
                            contentDescription = if (active) "Close search" else "Open search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onSearchSubmit()
                                onActiveChange(false)
                                focusManager.clearFocus()
                            },
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && !active) {
                                    onActiveChange(true)
                                }
                            },
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) {
                                    Text(
                                        text = "Search here",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    AnimatedVisibility(
                        visible = active || isSearching,
                        enter = fadeIn(animationSpec = tween(140, delayMillis = 90)),
                        exit = fadeOut(animationSpec = tween(90)),
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (query.isNotEmpty()) {
                                        onQueryChange("")
                                    } else {
                                        onActiveChange(false)
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !active && !isSearching,
                        enter = fadeIn(animationSpec = tween(140, delayMillis = 90)),
                        exit = fadeOut(animationSpec = tween(90)),
                    ) {
                        ProfileAvatar(
                            circleSize = 40,
                            onClick = onOpenSettings,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = active && searchResults.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    enter = fadeIn(animationSpec = tween(180, delayMillis = 140, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 8 },
                            animationSpec = tween(260, delayMillis = 90, easing = FastOutSlowInEasing),
                        ),
                    exit = fadeOut(animationSpec = tween(90, easing = FastOutSlowInEasing)) +
                        shrinkVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(top = 10.dp, start = 8.dp, end = 8.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(searchResults) { result ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = result.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                supportingContent = {
                                    val address = listOfNotNull(result.city, result.state).joinToString(", ")
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (address.isNotEmpty()) {
                                            Text(
                                                text = address,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        SearchResultMetrics(result)
                                    }
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        onResultClick(result)
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val SearchTransitionDurationMillis = 420

@Composable
private fun SearchResultMetrics(result: PhotonFeature) {
    val distance = result.driveDistanceMeters ?: result.straightLineDistanceMeters
    val duration = result.driveDurationSeconds

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (duration != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.DriveEta,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (distance != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Straight,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDistance(distance),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@Composable
fun TripSummaryCard(
    state: MapsUiState,
    onRetry: () -> Unit,
    onDirectionsClick: () -> Unit,
    onClearRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoadingRoute) {
        if (state.isLoadingRoute) {
            delay(500)
            showIndicator = true
        } else {
            showIndicator = false
        }
    }

    if (state.address != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!state.isLoadingRoute) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.address,
                            modifier = Modifier.weight(1f),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        FilledIconButton(
                            onClick = { onClearRoute() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }


                if (state.isLoadingRoute) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(end = 4.dp))
                        Text("")
                    }
                }

                state.routeError?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Retry", modifier = Modifier.clickable { onRetry() }, color = MaterialTheme.colorScheme.primary)
                        Text("Clear", modifier = Modifier.clickable { onClearRoute() }, color = MaterialTheme.colorScheme.primary)
                    }
                }

                val displayedDistance = state.remainingDistanceMeters ?: state.distanceMeters
                val displayedDuration = state.remainingDurationSeconds ?: state.durationSeconds
                if (!state.isLoadingRoute) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        Button(
                            onClick = onDirectionsClick,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon (
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Start",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,)
                        }

                        Button(
                            onClick = {  },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        ) {
                            Icon (
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Safe Start",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }


                    }
                    if (displayedDistance != null && displayedDuration != null) {
                        Text(
                            text = "${formatDistance(displayedDistance)} • ${formatDuration(displayedDuration)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val warningCount = state.routeWarningCount
                            val warningError = state.routeWarningError
                            val warningColor = when {
                                warningError != null -> MaterialTheme.colorScheme.onSurfaceVariant
                                warningCount == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                warningCount == 0 -> ExtendedColors().alertClearContent
                                else -> MaterialTheme.colorScheme.error
                            }

                            if (warningCount == null && warningError == null) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = warningColor,
                                )
                            } else {
                                Icon(
                                    imageVector = if (warningCount == 0) Icons.Outlined.Check else Icons.Outlined.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = warningColor,
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    warningError != null -> warningError
                                    warningCount == null -> "Checking warnings en route"
                                    warningCount == 0 -> "No warnings en route"
                                    warningCount == 1 -> "1 warning en route"
                                    else -> "$warningCount warnings en route"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = warningColor,
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun NavigationModeHeader(
    instruction: String,
    onExitNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ExtendedColors()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 0.dp, start = 2.dp, end = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.routingHeader),
    ) {
        val directionIcon = when {
            instruction.startsWith("Turn left", ignoreCase = true) -> Icons.Filled.TurnLeft
            instruction.startsWith("Turn right", ignoreCase = true) -> Icons.Filled.TurnRight
            instruction.startsWith("Head", ignoreCase = true) -> Icons.Filled.Straight
            else -> Icons.Filled.Navigation
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = directionIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 6.dp, bottom = 4.dp),
            )
            Text(
                text = instruction,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun NavigationModeFooter(
    remainingDistanceMeters: Double?,
    remainingDurationSeconds: Double?,
    modifier: Modifier = Modifier,
    onExitNavigation: () -> Unit,
) {

    if (remainingDistanceMeters == null || remainingDurationSeconds == null) return
    val eta = remember(remainingDurationSeconds) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, remainingDurationSeconds.toInt())
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 25.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column() {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DriveEta,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = formatDuration(remainingDurationSeconds),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 23.sp,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDistance(remainingDistanceMeters),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp,

                        )
                    Text(" • ")
                    Text(
                        text = "Arriving $eta",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp,
                    )
                }

            }
            val colors = ExtendedColors()
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onExitNavigation,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.exitContainer,
                    contentColor = colors.exitText
                )
            ) {
                Text(
                    "Exit",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium,)
            }

        }
    }
}
