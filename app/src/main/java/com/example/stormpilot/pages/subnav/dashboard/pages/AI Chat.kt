package com.example.stormpilot.pages.subnav.dashboard.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stormpilot.genai.GenAIViewModel
import com.example.stormpilot.pages.subnav.dashboard.aichat.ChatPanel

@Composable
fun AIChat(title: String, genAIViewModel: GenAIViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ChatPanel(genAIViewModel)
    }
}