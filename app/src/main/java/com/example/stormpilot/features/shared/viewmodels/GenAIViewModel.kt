package com.example.stormpilot.features.shared.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.features.shared.data.assistant.GenAIWeatherTools
import com.example.stormpilot.features.dashboard.ui.aichat.ChatMessage
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import com.example.stormpilot.features.shared.data.location.LocationRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Manages the storm assistant chat session and streams model responses into Compose-friendly message state.
 */
@HiltViewModel
class GenAIViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val stormPilotApi: StormPilotApi,
) : ViewModel() {

    val chatMessages = mutableStateListOf<ChatMessage>()

    // 1. Optimize configuration to minimize model thinking overhead
    private val modelConfig = generationConfig {
        temperature = 0.65f
        maxOutputTokens = 500
        topK = 20
    }

    private lateinit var chatSession: Chat

    init {
        chatSession = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-3.1-flash-lite",
                generationConfig = modelConfig,
                tools = listOf(GenAIWeatherTools(locationRepository, stormPilotApi).tool()),
                systemInstruction = content {
                    text(
                        "You are a concise storm chasing assistant. Keep all responses under 3 sentences unless the user explicitly asks for detail. " +
                            "Use the current-location tool when the user says here, near me, my location, or asks for local weather without naming a place. " +
                            "Use the available weather tools when the user asks about current weather, hourly timing, forecasts, alerts, convective risk, storm ingredients, or outlooks for a location. " +
                            "Ask for a location if the user asks for location-specific weather and does not provide one."
                    )
                }
            )
            .startChat()
    }


    /**
     * Streams an AI response turn-by-turn.
     * Updates the text inside the chatMessages list in real-time as chunks arrive.
     *
     * @param prompt The string request from the user.
     * @param loadingMessageId The unique id of the placeholder message already present in the UI.
     */
    suspend fun promptTestStream(prompt: String, loadingMessageId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val sb = StringBuilder()

                // 2. Call the streaming API endpoint instead of the standard blocking send
                chatSession.sendMessageStream(prompt).collect { chunk ->
                    val chunkText = chunk.text ?: ""
                    if (chunkText.isBlank()) return@collect

                    sb.append(chunkText)
                    updateAssistantMessage(
                        loadingMessageId = loadingMessageId,
                        text = sb.toString(),
                        isLoading = false
                    )
                }

                if (sb.isBlank()) {
                    updateAssistantMessage(
                        loadingMessageId = loadingMessageId,
                        text = "I couldn't generate a response for that. Try adding a specific location or asking again.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Walk the cause chain to find the most useful message
                val rootCause = generateSequence(e as Throwable) { it.cause }
                    .lastOrNull { it.message != null }

                val errorMsg = rootCause?.message
                    ?: e.localizedMessage
                    ?: "Connection lost."

                Log.e("GenAIViewModel", "Error type: ${e::class.simpleName}")
                Log.e("GenAIViewModel", "Root cause type: ${rootCause?.javaClass?.simpleName}")
                Log.e("GenAIViewModel", "Root cause message: $errorMsg", e)

                updateAssistantMessage(
                    loadingMessageId = loadingMessageId,
                    text = "Error: $errorMsg",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun updateAssistantMessage(
        loadingMessageId: Long,
        text: String,
        isLoading: Boolean
    ) = withContext(Dispatchers.Main.immediate) {
        val index = chatMessages.indexOfFirst { it.id == loadingMessageId }
        if (index >= 0) {
            chatMessages[index] = chatMessages[index].copy(
                text = text,
                isLoading = isLoading
            )
        }
    }

    fun sendMessage(prompt: String) {
        val loadingMessage = ChatMessage(
            text = null,
            isUser = false,
            isLoading = true
        )

        chatMessages.add(ChatMessage(text = prompt, isUser = true))
        chatMessages.add(loadingMessage)

        viewModelScope.launch {
            promptTestStream(prompt, loadingMessage.id)
        }
    }
}
