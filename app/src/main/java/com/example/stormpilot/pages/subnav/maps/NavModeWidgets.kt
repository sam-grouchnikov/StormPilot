package com.example.stormpilot.pages.subnav.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.pages.subnav.maps.routing.formatDistance
import com.example.stormpilot.pages.subnav.maps.routing.formatDuration
import com.example.stormpilot.pages.subnav.maps.viewmodel.MapsUiState
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScaffold(
    modifier: Modifier = Modifier,
    query: String,
    active: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onResultClick: (String) -> Unit,
) {
    SearchBar(
        modifier = if (!active) {
            modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 2.dp)
                .height(60.dp)
        } else {
            modifier
        },
        shape = RoundedCornerShape(30.dp),
        query = query,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.inverseOnSurface),
        onQueryChange = onQueryChange,
        onSearch = { onActiveChange(false) },
        windowInsets = WindowInsets(0, 0, 0, 0),
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Search here") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (active) {
                IconButton(onClick = { if (query.isNotEmpty()) onQueryChange("") else onActiveChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
    ) {
        AnimatedVisibility(
            visible = active,
            enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                expandVertically(animationSpec = tween(260, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)),
        ) {
            val recents = List(3) { index -> "Recent Location $index" }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = 10.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(recents) { result ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "123 Street Name, City",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.History,
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
                            .clickable { onResultClick(result) },
                    )
                }
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
    warningCount: Int,
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
            shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!state.isLoadingRoute) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.address,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Medium,
                        )

                        FilledIconButton(
                            onClick = { onClearRoute() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(30.dp),
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
                            onClick = { /*TODO*/ },
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
                            Icon(
                                imageVector = if (warningCount == 0) Icons.Outlined.Check else Icons.Outlined.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (warningCount == 0) Color(0xFF6CBE6C) else Color(0xFFBE746C),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (warningCount == 0) "No warnings en route" else "$warningCount warning(s) en route",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (warningCount == 0) Color(0xFF6CBE6C) else Color(0xFFBE746C),
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp, start = 2.dp, end = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 6.dp, bottom = 4.dp),
            )
            Text(
                text = instruction,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
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
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.SECOND, remainingDurationSeconds.toInt())
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onExitNavigation,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
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
