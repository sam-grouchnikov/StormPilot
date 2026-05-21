package com.example.stormpilot.pages.subnav.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.compose.StormPilotTheme
import com.example.stormpilot.viewmodel.AlertsViewModel
import com.example.stormpilot.genai.GenAIViewModel
import com.example.stormpilot.pages.subnav.dashboard.aichat.ChatPopup
import kotlinx.coroutines.delay
import com.example.stormpilot.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    genAIViewModel: GenAIViewModel = hiltViewModel()
) {
    val showWidgets = remember { mutableStateOf(false) }

    // Inside Dashboard Composable
    var showChat by remember { mutableStateOf(false) }



    StormPilotTheme {
        Column(modifier = Modifier.padding(horizontal = 0.dp),
            verticalArrangement = Arrangement.Top) {
            TopIconRow()

            ModernBubbleNavBarScreen(
                showChat = showChat,
                onChatClick = { showChat = true }
            )

            if (showChat) {
                ChatPopup({ showChat = false }, genAIViewModel)
            }
        }
    }
}

@Composable
fun TopIconRow() {
    val userName = "Sam"
    val avatarLetter = userName.firstOrNull()?.toString() ?: "?"
    val avatarColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val outlineColor = Color(0xFF1E885C)

    Row(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
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
                .size(40.dp)
                .padding(top = 3.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = avatarColor, shape = CircleShape)
                .border(
                    border = BorderStroke(width = 2.0.dp, color = outlineColor),
                    shape = CircleShape
                )
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarLetter,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
