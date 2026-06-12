package com.example.stormpilot.features.map.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.shared.data.routing.formatDistance
import com.example.stormpilot.features.shared.data.routing.formatDuration
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsBottomSheet(
    results: List<PhotonFeature>,
    isDarkMode: Boolean,
    containerColor: Color,
    onDismiss: () -> Unit,
    onResultFocused: (PhotonFeature) -> Unit,
    onDirectionsClick: (PhotonFeature) -> Unit,
) {
    val visibleResults = results.take(5)
    if (visibleResults.isEmpty()) return

    val sheetColors = searchResultsSheetColors(
        isDarkMode = isDarkMode,
        containerColor = containerColor,
    )
    val pagerState = rememberPagerState(pageCount = { visibleResults.size })

    LaunchedEffect(visibleResults) {
        pagerState.scrollToPage(0)
        onResultFocused(visibleResults.first())
    }

    LaunchedEffect(pagerState, visibleResults) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                visibleResults.getOrNull(page)?.let(onResultFocused)
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = sheetColors.container,
        contentColor = sheetColors.content,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = sheetColors.dragHandle)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                contentPadding = PaddingValues(horizontal = 28.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val result = visibleResults[page]
                SearchResultCard(
                    result = result,
                    isDarkMode = isDarkMode,
                    onDirectionsClick = { onDirectionsClick(result) },
                )
            }

            SearchResultPageIndicator(
                pageCount = visibleResults.size,
                selectedPage = pagerState.currentPage.coerceIn(0, visibleResults.lastIndex),
                isDarkMode = isDarkMode,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: PhotonFeature,
    isDarkMode: Boolean,
    onDirectionsClick: () -> Unit,
) {
    val cardColors = searchResultCardColors(isDarkMode)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        shape = RoundedCornerShape(20.dp),
        color = cardColors.container,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = cardColors.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = result.displayAddress(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cardColors.address,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    result.displayCategory()?.let { category ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = cardColors.category,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricChip(
                    label = result.driveDurationSeconds?.let(::formatDuration) ?: "No drive time",
                    icon = Icons.Filled.DriveEta,
                    colors = cardColors,
                    modifier = Modifier.weight(1f),
                )
                MetricChip(
                    label = result.driveDistanceMeters
                        ?.let(::formatDistance)
                        ?: result.straightLineDistanceMeters?.let(::formatDistance)
                        ?: "No distance",
                    icon = Icons.Filled.Straight,
                    colors = cardColors,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = onDirectionsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cardColors.buttonContainer,
                    contentColor = cardColors.buttonContent,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.NearMe,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Directions",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchResultPageIndicator(
    pageCount: Int,
    selectedPage: Int,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardColors = searchResultCardColors(isDarkMode)

    Row(
        modifier = modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val isSelected = page == selectedPage
            Surface(
                modifier = Modifier.size(
                    width = if (isSelected) 18.dp else 6.dp,
                    height = 6.dp,
                ),
                shape = CircleShape,
                color = if (isSelected) {
                    cardColors.indicatorSelected
                } else {
                    cardColors.indicatorUnselected
                },
            ) {}
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: SearchResultCardColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.metricContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = colors.metricContent,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.metricContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class SearchResultsSheetColors(
    val container: Color,
    val content: Color,
    val dragHandle: Color,
)

@Composable
private fun searchResultsSheetColors(
    isDarkMode: Boolean,
    containerColor: Color,
): SearchResultsSheetColors {
    val scheme = MaterialTheme.colorScheme
    return if (isDarkMode) {
        SearchResultsSheetColors(
            container = containerColor,
            content = scheme.onSurface,
            dragHandle = scheme.outlineVariant,
        )
    } else {
        SearchResultsSheetColors(
            container = containerColor,
            content = scheme.onSurface,
            dragHandle = scheme.outline,
        )
    }
}

private data class SearchResultCardColors(
    val container: Color,
    val title: Color,
    val address: Color,
    val category: Color,
    val metricContainer: Color,
    val metricContent: Color,
    val buttonContainer: Color,
    val buttonContent: Color,
    val indicatorSelected: Color,
    val indicatorUnselected: Color,
)

@Composable
private fun searchResultCardColors(isDarkMode: Boolean): SearchResultCardColors {
    val scheme = MaterialTheme.colorScheme
    return if (isDarkMode) {
        SearchResultCardColors(
            container = scheme.surfaceContainerHigh,
            title = scheme.onSurface,
            address = scheme.onSurfaceVariant,
            category = scheme.tertiary,
            metricContainer = scheme.surface,
            metricContent = scheme.onSurfaceVariant,
            buttonContainer = scheme.primaryContainer,
            buttonContent = scheme.onPrimaryContainer,
            indicatorSelected = scheme.primary,
            indicatorUnselected = scheme.outlineVariant,
        )
    } else {
        SearchResultCardColors(
            container = scheme.surface,
            title = scheme.onSurface,
            address = scheme.onSurfaceVariant,
            category = scheme.secondary,
            metricContainer = scheme.surfaceContainerLowest.copy(alpha = 0.7f),
            metricContent = scheme.onSurfaceVariant,
            buttonContainer = scheme.primary,
            buttonContent = scheme.onPrimary,
            indicatorSelected = scheme.primary,
            indicatorUnselected = scheme.outlineVariant,
        )
    }
}
