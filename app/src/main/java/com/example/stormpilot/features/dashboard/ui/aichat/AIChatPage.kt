package com.example.stormpilot.features.dashboard.ui.aichat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.common.ui.AnimatedStormAiChatBackdrop
import com.example.stormpilot.features.common.ui.AnimatedStormAiShadowContainer
import com.example.stormpilot.features.shared.viewmodels.GenAIViewModel

@Composable
fun AIChat(title: String, genAIViewModel: GenAIViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedStormAiChatBackdrop(
            visible = true,
            modifier = Modifier.fillMaxSize(),
        )

        // The Popup Container
        AnimatedStormAiShadowContainer(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Slightly smaller width to allow more shadow room
                .fillMaxHeight(0.75f), // Slightly smaller height
            cornerRadius = 24.dp,
            blurRadius = 20.dp,
            shadowPadding = 0.dp,
            drawBorder = true,
        ) {
            // The actual content surface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.5.dp) // Thinner 1.5.dp border
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(22.5.dp)) // Dark background for the popup
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    // Additional chat UI could go here
                }
            }
        }
    }
}
