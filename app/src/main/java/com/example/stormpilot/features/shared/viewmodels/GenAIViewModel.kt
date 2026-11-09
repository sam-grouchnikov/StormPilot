package com.example.stormpilot.features.shared.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stormpilot.core.StormAiAction
import com.example.stormpilot.core.StormAiRepository
import com.example.stormpilot.features.ai.ui.chat.ChatMessage
import com.example.stormpilot.features.shared.data.location.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Manages the StormAI chat state and sends prompts through the Spring Boot assistant backend.
 */
@HiltViewModel
class GenAIViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val stormAiRepository: StormAiRepository,
) : ViewModel() {

    val chatMessages = mutableStateListOf<ChatMessage>()

    /**
     * Sends an AI response request through the same StormAI backend used by the map page.
     * Updates the placeholder message once the Spring Boot backend returns.
     *
     * @param prompt The string request from the user.
     * @param loadingMessageId The unique id of the placeholder message already present in the UI.
     */
    suspend fun promptTestStream(
        prompt: String,
        loadingMessageId: Long,
        onActions: (List<StormAiAction>) -> Unit = {},
    ) {
        try {
            val location = resolveAssistantLocationParam()
            stormAiRepository.submitRequest(input = prompt, location = location)
                .onSuccess { response ->
                    updateAssistantMessage(
                        loadingMessageId = loadingMessageId,
                        text = response.reply.ifBlank {
                            "I couldn't generate a response for that. Try adding a specific location or asking again."
                        },
                        isLoading = false,
                    )
                    if (response.actions.isNotEmpty()) {
                        withContext(Dispatchers.Main.immediate) {
                            onActions(response.actions)
                        }
                    }
                }
                .onFailure { error ->
                    updateAssistantMessage(
                        loadingMessageId = loadingMessageId,
                        text = "Error: ${error.readableMessage()}",
                        isLoading = false,
                    )
                }
        } catch (e: Exception) {
            val errorMsg = e.readableMessage()

            Log.e("GenAIViewModel", "StormAI request failed: $errorMsg", e)

            updateAssistantMessage(
                loadingMessageId = loadingMessageId,
                text = "Error: $errorMsg",
                isLoading = false,
            )
        }
    }

    private suspend fun resolveAssistantLocationParam(): String {
        val location = locationRepository.location.value
            ?: runCatching { locationRepository.fetchCurrentLocation() }.getOrNull()

        return location?.let {
            String.format(Locale.US, "%.6f,%.6f", it.latitude, it.longitude)
        } ?: UNKNOWN_LOCATION
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

    fun sendMessage(
        prompt: String,
        onActions: (List<StormAiAction>) -> Unit = {},
    ) {
        val loadingMessage = ChatMessage(
            text = null,
            isUser = false,
            isLoading = true
        )

        chatMessages.add(ChatMessage(text = prompt, isUser = true))
        chatMessages.add(loadingMessage)

        viewModelScope.launch {
            promptTestStream(
                prompt = prompt,
                loadingMessageId = loadingMessage.id,
                onActions = onActions,
            )
        }
    }

    private fun Throwable.readableMessage(): String {
        val rootCause = generateSequence(this) { it.cause }
            .lastOrNull { it.message != null }

        return rootCause?.message
            ?: localizedMessage
            ?: "StormAI request failed."
    }

    private companion object {
        const val UNKNOWN_LOCATION = "unknown"
    }
}
