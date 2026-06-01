package com.example.stormpilot.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.disableHotReloadMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.core.isAppInAvoidStormMode
import com.example.stormpilot.core.isAppInDarkMode
import com.example.stormpilot.core.updateAppCompatNightMode
import com.example.stormpilot.features.common.ui.ProfileAvatar
import com.example.stormpilot.features.settings.ui.rows.ChangePasswordRow
import com.example.stormpilot.features.settings.ui.rows.ChangeUserRow
import com.example.stormpilot.features.settings.ui.rows.LogOutRow
import com.example.stormpilot.features.settings.ui.rows.SmartRoutingRow
import com.example.stormpilot.features.settings.ui.rows.ThemeSettingRow

@Composable
fun SettingsPage(
    onDismiss: () -> Unit = {},
) {
    val isDarkMode = isAppInDarkMode()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            SettingsHeader(onDismiss = onDismiss)


            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(circleSize = 60, textSize = 25, onClick = {})
                Spacer(modifier = Modifier.height(13.dp))
                Text(
                    "sam.grouchnikov@gmail.com",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }





            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 5.dp, top = 18.dp))

            Text(
                text = "General Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top =20.dp, bottom = 5.dp),
            )

            ThemeSettingRow(
                checked = isDarkMode,
                onCheckedChange = { checked ->
                    val mode = if (checked) "dark" else "light"
                    AppSettings.mode = mode
                    updateAppCompatNightMode(mode)
                },
            )

            var smartRouting = isAppInAvoidStormMode()

            SmartRoutingRow(
                checked = smartRouting,
                onCheckedChange = { checked ->
                    AppSettings.avoidStorms = checked
                    smartRouting = !smartRouting
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 5.dp, top = 0.dp))

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top =20.dp, bottom = 10.dp),
            )

            ChangeUserRow()
            ChangePasswordRow()
            LogOutRow()
        }
    }
}

@Composable
private fun SettingsHeader(
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(end = 12.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close settings",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
