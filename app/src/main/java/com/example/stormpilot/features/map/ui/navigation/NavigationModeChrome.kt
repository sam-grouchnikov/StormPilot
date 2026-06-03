package com.example.stormpilot.features.map.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.map.data.routing.formatDistance
import com.example.stormpilot.features.map.data.routing.formatDuration
import com.example.stormpilot.ui.theme.ExtendedColors
import java.util.Calendar

@Composable
fun NavigationModeHeader(
    instruction: String,
    onExitNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ExtendedColors()
    val directionIcon = when {
        instruction.startsWith("Turn left", ignoreCase = true) -> Icons.Filled.TurnLeft
        instruction.startsWith("Turn right", ignoreCase = true) -> Icons.Filled.TurnRight
        instruction.startsWith("Head", ignoreCase = true) -> Icons.Filled.Straight
        else -> Icons.Filled.Navigation
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.routingHeader),
    ) {
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
                color = Color.White,
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
    val colors = ExtendedColors()

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
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DriveEta,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(25.dp),
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
                    containerColor = colors.exitContainer,
                    contentColor = colors.exitText,
                ),
            ) {
                Text(
                    text = "Exit",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
