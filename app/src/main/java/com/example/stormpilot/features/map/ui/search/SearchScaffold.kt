package com.example.stormpilot.features.map.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.outlined.ExpandLess
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.navigation.ui.components.coloredShadow
import com.example.stormpilot.features.shared.data.routing.formatDistance
import com.example.stormpilot.features.shared.data.routing.formatDuration
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.ui.theme.extendedColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

private const val SearchTransitionDurationMillis = 420
private const val MapActionsFadeDurationMillis = 145
private const val MapActionsFadeOutDurationMillis = 218
private const val MapActionsStaggerMillis = 50
private val SearchBarHeight = 60.dp
private val SearchBarTopSpacing = 6.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScaffold(
    modifier: Modifier = Modifier,
    query: String,
    active: Boolean,
    searchResults: List<PhotonFeature>,
    recentSearches: List<PhotonFeature>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSearchSubmit: () -> Unit,
    onResultClick: (PhotonFeature) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenStormAiChat: () -> Unit = {},
    containerColor: Color,
    mapOverlayControls: @Composable () -> Unit,
) {
    var mapActionsExpanded by rememberSaveable { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val searchListState = rememberLazyListState()
    val recentResults = if (query.isBlank()) recentSearches.take(2) else emptyList()
    val topSearchResults = if (query.isNotBlank()) searchResults.take(3) else emptyList()
    val remainingSearchResults = if (query.isNotBlank()) searchResults.drop(3) else emptyList()
    val statusBarTopPadding = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val surfaceTopPadding by animateDpAsState(
        targetValue = if (active) 0.dp else statusBarTopPadding + SearchBarTopSpacing,
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

    LaunchedEffect(query, topSearchResults) {
        if (query.isNotBlank() && topSearchResults.isNotEmpty()) {
            searchListState.scrollToItem(0)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val surfaceHeight by animateDpAsState(
            targetValue = if (active) maxHeight else SearchBarHeight,
            animationSpec = tween(SearchTransitionDurationMillis, easing = FastOutSlowInEasing),
            label = "maps_search_height",
        )

        if (!active) {
            val overshootPx = with(density) { 4.dp.roundToPx() }
            val mapActionsLayoutDuration = MapActionsFadeDurationMillis + MapActionsStaggerMillis
            val chevronAlpha = remember { Animatable(1f) }
            LaunchedEffect(chevronAlpha) {
                snapshotFlow { mapActionsExpanded }.drop(1).collectLatest { expanded ->
                    if (expanded) {
                        chevronAlpha.animateTo(1f, tween(MapActionsFadeDurationMillis / 2))
                    } else {
                        chevronAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = keyframes {
                                durationMillis = MapActionsFadeOutDurationMillis / 2 +
                                    (MapActionsFadeDurationMillis + 1) / 2
                                0f at (MapActionsFadeOutDurationMillis / 2)
                            },
                        )
                    }
                }
            }
            val chevronRotation = animateFloatAsState(
                targetValue = if (mapActionsExpanded) 0f else 180f,
                animationSpec = tween(MapActionsFadeDurationMillis, easing = LinearOutSlowInEasing),
                label = "map_actions_chevron_rotation",
            )
            val chevronOffset = animateIntAsState(
                targetValue = if (mapActionsExpanded) 0 else 16,
                animationSpec = if (mapActionsExpanded) {
                    keyframes {
                        durationMillis = mapActionsLayoutDuration + 90
                        overshootPx at mapActionsLayoutDuration using FastOutSlowInEasing
                    }
                } else {
                    tween(mapActionsLayoutDuration)
                },
                label = "map_actions_chevron_offset",
            )

            Column(
                modifier = Modifier
                    .padding(top = statusBarTopPadding + SearchBarTopSpacing + SearchBarHeight)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                AnimatedVisibility(
                    visible = mapActionsExpanded,
                    enter = expandVertically(tween(MapActionsFadeDurationMillis), expandFrom = Alignment.Top),
                    exit = fadeOut(
                        tween(MapActionsFadeOutDurationMillis, MapActionsStaggerMillis, LinearOutSlowInEasing),
                    ) +
                        shrinkVertically(
                            tween(MapActionsFadeOutDurationMillis, delayMillis = MapActionsStaggerMillis),
                            shrinkTowards = Alignment.Top,
                        ),
                ) {
                    Box(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                        SearchShortcutRow(
                            containerColor = containerColor,
                            onOpenStormAiChat = onOpenStormAiChat,
                            onPlaceSearch = { shortcutQuery ->
                                onQueryChange(shortcutQuery)
                                onSearchSubmit()
                            },
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = mapActionsExpanded,
                        enter = fadeIn(tween(MapActionsFadeDurationMillis, delayMillis = MapActionsStaggerMillis)) +
                            expandVertically(
                                tween(MapActionsFadeDurationMillis, delayMillis = MapActionsStaggerMillis),
                                expandFrom = Alignment.Top,
                            ) +
                            slideInVertically(
                                animationSpec = keyframes {
                                    durationMillis = 210
                                    delayMillis = MapActionsStaggerMillis
                                    IntOffset(0, -2 * overshootPx) at 0 using LinearOutSlowInEasing
                                    IntOffset(0, overshootPx) at 120 using FastOutSlowInEasing
                                },
                                initialOffsetY = { -2 * overshootPx },
                            ),
                        exit = fadeOut(tween(MapActionsFadeOutDurationMillis, easing = LinearOutSlowInEasing)) +
                            shrinkVertically(tween(MapActionsFadeOutDurationMillis), shrinkTowards = Alignment.Top),
                    ) {
                        Box(modifier = Modifier.padding(bottom = 6.dp)) {
                            mapOverlayControls()
                        }
                    }
                    FilledIconButton(
                        onClick = { mapActionsExpanded = !mapActionsExpanded },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .offset { IntOffset(x = 0, y = chevronOffset.value) }
                            .graphicsLayer { alpha = chevronAlpha.value },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExpandLess,
                            contentDescription = if (mapActionsExpanded) "Hide map shortcuts and layers" else "Show map shortcuts and layers",
                            modifier = Modifier.rotate(chevronRotation.value),
                        )
                    }
                }
            }
        }

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
                        .height(SearchBarHeight)
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
                        AccountMenuAnchor(onClick = onOpenSettings)
                    }
                }

                AnimatedVisibility(
                    visible = active,
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
                        state = searchListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(top = 10.dp, start = 8.dp, end = 8.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        items(recentResults) { result ->
                            SearchResultListItem(
                                result = result,
                                onClick = {
                                    focusManager.clearFocus()
                                    onResultClick(result)
                                },
                            )
                        }

                        items(topSearchResults) { result ->
                            SearchResultListItem(
                                result = result,
                                onClick = {
                                    focusManager.clearFocus()
                                    onResultClick(result)
                                },
                            )
                        }

                        items(remainingSearchResults) { result ->
                            SearchResultListItem(
                                result = result,
                                onClick = {
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

@Composable
private fun SearchResultListItem(
    result: PhotonFeature,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = {
            val address = result.displayAddress()
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
            .clickable(onClick = onClick),
    )
}

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
