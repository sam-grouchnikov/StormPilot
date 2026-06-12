package com.example.stormpilot.features.navigation.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.stormpilot.features.common.ui.AnimatedStormAiShadowContainer
import com.example.stormpilot.ui.theme.ExtendedColors
import java.util.Locale

enum class StormAiSheetMode {
    Chat,
    Voice,
}

@Composable
fun StormAiVoiceButton(
    onOpenVoiceRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showAudioPermissionDialog by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onOpenVoiceRequest()
        } else {
            showAudioPermissionDialog = true
        }
    }

    fun openVoiceSheet() {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onOpenVoiceRequest()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val colors = ExtendedColors()

    AnimatedStormAiShadowContainer(modifier = modifier) {
        FilledIconButton(
            onClick = { openVoiceSheet() },
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 7.dp)
                .size(50.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.purpleSurfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Open StormAI voice request",
                modifier = Modifier.size(20.dp),
            )
        }
    }

    if (showAudioPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showAudioPermissionDialog = false },
            title = { Text(text = "Microphone unavailable") },
            text = { Text(text = "Enable microphone permission to use voice requests.") },
            confirmButton = {
                TextButton(onClick = { showAudioPermissionDialog = false }) {
                    Text(text = "OK")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StormAiRequestSheet(
    mode: StormAiSheetMode,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var requestText by remember(mode) { mutableStateOf("") }
    var isListening by remember(mode) { mutableStateOf(false) }
    var voiceStatus by remember(mode) {
        mutableStateOf(if (mode == StormAiSheetMode.Voice) "Listening" else "")
    }
    val recognitionAvailable = remember(context) {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
    val speechRecognizer = remember(context, mode, recognitionAvailable) {
        if (mode == StormAiSheetMode.Voice && recognitionAvailable) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    val recognitionIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun submitRequest() {
        val request = requestText.trim()
        if (request.isBlank() || isSubmitting) return

        speechRecognizer?.stopListening()
        focusManager.clearFocus()
        onSubmit(request)
    }

    fun startListening() {
        if (speechRecognizer == null) {
            voiceStatus = "Voice input unavailable"
            return
        }

        isListening = true
        voiceStatus = "Listening"
        speechRecognizer.startListening(recognitionIntent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        if (mode == StormAiSheetMode.Voice) {
            voiceStatus = "Processing"
        }
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer == null) {
            onDispose { }
        } else {
            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        voiceStatus = "Listening"
                    }

                    override fun onBeginningOfSpeech() {
                        voiceStatus = "Listening"
                    }

                    override fun onRmsChanged(rmsdB: Float) = Unit

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() {
                        isListening = false
                        voiceStatus = "Processing"
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        voiceStatus = speechErrorMessage(error)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        requestText = results.bestSpeechMatch().orEmpty()
                        voiceStatus = if (requestText.isBlank()) {
                            "No speech detected"
                        } else {
                            "Ready"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        partialResults.bestSpeechMatch()?.let { partialText ->
                            requestText = partialText
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                },
            )

            onDispose {
                speechRecognizer.cancel()
                speechRecognizer.destroy()
            }
        }
    }

    LaunchedEffect(mode, speechRecognizer) {
        if (mode == StormAiSheetMode.Voice) {
            startListening()
        }
    }

    val sendButtonScale by animateFloatAsState(
        targetValue = if (requestText.isNotBlank()) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "stormAiSendButtonScale",
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
    ) {
        AnimatedStormAiShadowContainer(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
            cornerRadius = 24.dp,
            blurRadius = 20.dp,
            shadowPadding = 0.dp,
            drawBorder = true,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.5.dp),
                shape = RoundedCornerShape(22.5.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 12.dp,
                shadowElevation = 18.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp),
                ) {
                    StormAiSheetHeader(
                        mode = mode,
                        status = if (isSubmitting) "Thinking" else voiceStatus,
                        isSubmitting = isSubmitting,
                        onDismiss = onDismiss,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            OutlinedTextField(
                                value = requestText,
                                onValueChange = { requestText = it },
                                enabled = !isSubmitting,
                                placeholder = {
                                    Text(
                                        text = if (mode == StormAiSheetMode.Chat) {
                                            "Type a StormAI request"
                                        } else {
                                            "Voice transcript"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { submitRequest() }),
                            )

                            if (mode == StormAiSheetMode.Voice) {
                                Spacer(modifier = Modifier.width(6.dp))
                                FilledIconButton(
                                    onClick = {
                                        if (isListening) {
                                            stopListening()
                                        } else {
                                            startListening()
                                        }
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isListening) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        contentColor = if (isListening) {
                                            MaterialTheme.colorScheme.onError
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                    ),
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                        contentDescription = if (isListening) "Stop listening" else "Start listening",
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            FilledIconButton(
                                onClick = { submitRequest() },
                                enabled = requestText.isNotBlank() && !isSubmitting,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .scale(sendButtonScale),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                ),
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "Send StormAI request",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StormAiSheetHeader(
    mode: StormAiSheetMode,
    status: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ) {
            Icon(
                imageVector = if (mode == StormAiSheetMode.Chat) Icons.AutoMirrored.Rounded.Chat else Icons.Rounded.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(9.dp)
                    .size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (mode == StormAiSheetMode.Chat) "StormAI" else "Voice Request",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mode == StormAiSheetMode.Voice) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            enabled = !isSubmitting,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close StormAI request",
            )
        }
    }
}

private fun Bundle?.bestSpeechMatch(): String? {
    return this
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.firstOrNull()
        ?.trim()
}

private fun speechErrorMessage(error: Int): String {
    return when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio error"
        SpeechRecognizer.ERROR_CLIENT -> "Voice input stopped"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission unavailable"
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        -> "Network unavailable"
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
        -> "No speech detected"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Voice service unavailable"
        else -> "Voice input unavailable"
    }
}
