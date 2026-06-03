package com.example.stormpilot.features.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.features.alerts.presentation.AlertsViewModel
import com.example.stormpilot.features.assistant.presentation.GenAIViewModel
import com.example.stormpilot.features.dashboard.ui.aichat.ChatPopup
import com.example.stormpilot.R
import com.example.stormpilot.features.common.ui.AccountMenuAnchor

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    genAIViewModel: GenAIViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit = {},
) {
    val showWidgets = remember { mutableStateOf(false) }

    var showChat by remember { mutableStateOf(false) }



    StormPilotTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ModernBubbleNavBarScreen(
                showChat = showChat,
                onChatClick = { showChat = true }
            )

            TopIconRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                onOpenSettings = onOpenSettings,
            )

            if (showChat) {
                ChatPopup({ showChat = false }, genAIViewModel)
            }
        }
    }
}

@Composable
fun TopIconRow(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Description of the image for accessibility",
            modifier = Modifier
                .size(42.dp)
                .padding(top = 3.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Outlined.Notifications,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .padding(top = 3.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        AccountMenuAnchor(onClick = onOpenSettings)

    }
}
