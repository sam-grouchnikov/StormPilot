package com.example.stormpilot.features.dashboard.ui.aichat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.common.ui.AnimatedStormAiShadowContainer
import com.example.stormpilot.features.shared.viewmodels.GenAIViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ContextualChatPrompt(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPopup(onDismiss: () -> Unit, viewModel: GenAIViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }

    val chatMessages = viewModel.chatMessages
    val promptSuggestions = rememberContextualChatPrompts()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )

    fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.lastIndex)
            }
        }
    }

    fun sendMessage() {
        val prompt = inputText.trim()
        if (prompt.isBlank()) return

        inputText = ""
        focusManager.clearFocus()
        viewModel.sendMessage(prompt)
    }

    fun sendPromptSuggestion(prompt: String) {
        inputText = ""
        focusManager.clearFocus()
        viewModel.sendMessage(prompt)
    }

    LaunchedEffect(Unit) {
        if (chatMessages.isNotEmpty()) {
            listState.scrollToItem(chatMessages.lastIndex)
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            delay(40)
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    val sendButtonScale by animateFloatAsState(
        targetValue = if (inputText.isNotBlank()) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "sendButtonScale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
    ) {
//        AnimatedStormAiShadowContainer(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.88f)
//                .padding(horizontal = 10.dp, vertical = 12.dp)
//                .navigationBarsPadding()
//                .imePadding(),
//            cornerRadius = 30.dp,
//            blurRadius = 20.dp,
//            shadowPadding = 0.dp,
//            drawBorder = true,
//        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight(0.88f)
                    .fillMaxWidth()
                    .padding(1.5.dp),
                shape = RoundedCornerShape(28.5.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
                tonalElevation = 12.dp,
                shadowElevation = 18.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        ChatPopupHeader(onDismiss = onDismiss)

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (chatMessages.isEmpty()) {
                                item {
                                    EmptyChatState(
                                        prompts = promptSuggestions,
                                        onPromptClick = ::sendPromptSuggestion,
                                    )
                                }
                            } else {
                                items(
                                    items = chatMessages,
                                    key = { it.id }
                                ) { message ->
                                    ChatBubble(
                                        message = message,
                                        onAnimatedContentChanged = {
                                            if (chatMessages.lastOrNull()?.id == message.id) {
                                                scrollToBottom()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        if (chatMessages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ContextualPromptRow(
                                prompts = promptSuggestions,
                                onPromptClick = ::sendPromptSuggestion,
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        AnimatedStormAiShadowContainer() {


                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            "Ask StormPilot AI anything",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        disabledBorderColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                FilledIconButton(
                                    onClick = { sendMessage() },
                                    enabled = inputText.isNotBlank(),
                                    modifier = Modifier
                                        .padding(bottom = 4.dp)
                                        .scale(sendButtonScale),
                                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "Send prompt"
                                    )
                                }
                            }
                        }
                    }
                }
            }
//        }
    }
}

@Composable
private fun rememberContextualChatPrompts(): List<ContextualChatPrompt> = remember {
    listOf(
        ContextualChatPrompt(
            label = "Alert summary",
            prompt = "Summarize active weather alerts for my current location. Include severity, timing, and what I should do first.",
            icon = Icons.Outlined.WarningAmber,
        ),
        ContextualChatPrompt(
            label = "Weather brief",
            prompt = "Give me a concise weather brief for my current location, including current conditions, the next few hours, and anything that could affect travel.",
            icon = Icons.Outlined.Cloud,
        ),
        ContextualChatPrompt(
            label = "Storm setup",
            prompt = "Explain the current storm environment near me in plain language. Focus on instability, wind, moisture, and what risks they imply.",
            icon = Icons.Outlined.Thunderstorm,
        ),
        ContextualChatPrompt(
            label = "Travel timing",
            prompt = "Help me choose safer travel timing today based on current alerts and forecast trends near my location.",
            icon = Icons.Outlined.LocationOn,
        ),
    )
}

@Composable
private fun ChatPopupHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "StormPilot AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Chat with a weather-aware agent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close chat"
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun EmptyChatState(
    prompts: List<ContextualChatPrompt>,
    onPromptClick: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Ask for routing help, weather context, or quick planning.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            ContextualPromptRow(
                prompts = prompts,
                onPromptClick = onPromptClick,
            )

        }
    }
}

@Composable
private fun ContextualPromptRow(
    prompts: List<ContextualChatPrompt>,
    onPromptClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        prompts.forEach { prompt ->
            ContextualPromptChip(
                prompt = prompt,
                onClick = { onPromptClick(prompt.prompt) },
            )
        }
    }
}

@Composable
private fun ContextualPromptChip(
    prompt: ContextualChatPrompt,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = prompt.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = prompt.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
        }
    }
}
