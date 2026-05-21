package com.example.stormpilot.pages

import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.StormPilotTheme
import com.example.stormpilot.AppSettings
import com.example.stormpilot.updateAppCompatNightMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    var mode by remember { mutableStateOf(AppSettings.mode) }
    var showScale by remember { mutableStateOf("zoom") }
    var smartRerouting by remember { mutableStateOf("on") }

    var openDialog by remember { mutableStateOf<String?>(null) }

    StormPilotTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Settings") })
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                SettingsSectionHeader("Display")
                SettingsItem(
                    label = "Mode",
                    value = mapOf("light" to "Light", "dark" to "Dark", "system" to "System default")[mode]!!,
                    onClick = { openDialog = "mode" }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    label = "Show scale on map",
                    value = mapOf("zoom" to "When zooming", "always" to "Always", "never" to "Never")[showScale]!!,
                    onClick = { openDialog = "scale" }
                )
                SettingsSectionHeader("Navigation")
                SettingsItem(
                    label = "Smart rerouting",
                    value = if (smartRerouting == "on") "On" else "Off",
                    onClick = { openDialog = "rerouting" }
                )
                SettingsSectionHeader("Account")
                SettingsItem(
                    label = "Profile",
                    value = "Manage your account",
                    onClick = { openDialog = "profile" }
                )
            }
        }

        // Dialogs
        when (openDialog) {
            "mode" -> RadioDialog(
                title = "Mode",
                options = listOf("light" to "Light", "dark" to "Dark", "system" to "System default"),
                selected = mode,
                onSelect = { mode = it; AppSettings.mode = it; updateAppCompatNightMode(it); openDialog = null },
                onDismiss = { openDialog = null }
            )
            "scale" -> RadioDialog(
                title = "Show scale on map",
                options = listOf("zoom" to "When zooming", "always" to "Always", "never" to "Never"),
                selected = showScale,
                onSelect = { showScale = it; openDialog = null },
                onDismiss = { openDialog = null }
            )
            "rerouting" -> RadioDialog(
                title = "Smart rerouting",
                options = listOf("on" to "On", "off" to "Off"),
                selected = smartRerouting,
                onSelect = { smartRerouting = it; openDialog = null },
                onDismiss = { openDialog = null }
            )
            "profile" -> ProfileDialog(
                onDismiss = { openDialog = null }
            )
        }
    }
}

@Composable
fun RadioDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 3.dp)
                    ) {
                        RadioButton(selected = selected == key, onClick = { onSelect(key) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(35.dp))
        },
        title = { Text("Account Username") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                TextButton(
                    onClick = { /* handle change password */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Change Password",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider()
                TextButton(
                    onClick = { /* handle log out */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}