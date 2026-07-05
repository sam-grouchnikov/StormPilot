package com.example.stormpilot.features.ai.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stormpilot.ui.theme.extendedColors
import kotlinx.coroutines.delay

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String?,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

@Composable
fun ChatBubble(
    message: ChatMessage,
    onAnimatedContentChanged: () -> Unit
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = RoundedCornerShape(22.dp)

    val bubbleBrush = if (isUser) {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primaryContainer
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }

    var visible by remember(message.id) { mutableStateOf(false) }

    LaunchedEffect(message.id) {
        visible = true
        onAnimatedContentChanged()
    }

    val shape = if (isUser) RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
    else RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(360, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clip(shape)
                    .background(bubbleBrush)
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                if (!isUser) {
                    Text(
                        text = if (message.isLoading) "StormPilot AI is thinking" else "StormPilot AI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.extendedColors.theme.vibrantPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Crossfade(
                    targetState = message.isLoading,
                    animationSpec = tween(durationMillis = 220),
                    label = "messageState"
                ) { loading ->
                    if (loading) {
                        ThinkingIndicator()
                    } else {
                        val textColor = if (isUser) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        if (isUser) {
                            Text(
                                text = message.text.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        } else {
                            AnimatedAssistantText(
                                text = message.text.orEmpty(),
                                textColor = textColor,
                                onSegmentShown = onAnimatedContentChanged
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedAssistantText(
    text: String,
    textColor: Color,
    onSegmentShown: () -> Unit
) {
    val segments = remember(text) { buildRevealSegments(text) }
    var visibleSegments by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleSegments = 0
        segments.forEachIndexed { index, _ ->
            delay(if (index == 0) 0L else 15L)
            visibleSegments = index + 1
            onSegmentShown()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        segments.forEachIndexed { index, segment ->
            AnimatedVisibility(
                visible = index < visibleSegments,
                enter = fadeIn(animationSpec = tween(220)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        )
            ) {
                Text(
                    text = segment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val shimmerShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ),
        label = "shimmerShift"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.extendedColors.theme.vibrantPrimary.copy(alpha = 0.10f),
            MaterialTheme.extendedColors.theme.vibrantPrimary.copy(alpha = 0.30f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        ),
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(260f * shimmerShift + 120f, 0f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .width(132.dp)
                .height(8.dp)
                .clip(CircleShape)
                .background(shimmerBrush)
        )
    }
}

fun buildRevealSegments(text: String): List<String> {
    val paragraphs = text
        .trim()
        .split('\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (paragraphs.isEmpty()) return listOf("")

    return paragraphs.flatMap { paragraph ->
        val sentences = Regex("(?<=[.!?])\\s+")
            .split(paragraph)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (sentences.size <= 1) {
            listOf(paragraph)
        } else {
            val chunks = mutableListOf<String>()
            val builder = StringBuilder()

            sentences.forEach { sentence ->
                if (builder.isEmpty()) {
                    builder.append(sentence)
                } else if (builder.length + sentence.length + 1 <= 120) {
                    builder.append(' ').append(sentence)
                } else {
                    chunks += builder.toString()
                    builder.clear()
                    builder.append(sentence)
                }
            }

            if (builder.isNotEmpty()) chunks += builder.toString()

            chunks
        }
    }
}
