package com.example.stormpilot.genai

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.stormpilot.pages.subnav.dashboard.aichat.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GenAIViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // 1. Thread-safe snapshot list mapping directly to the ChatPopup layout
    val chatMessages = mutableStateListOf<ChatMessage>()

    // 2. Setup the stable core Firebase AI production model
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName = "gemini-2.5-flash")

    // 3. Thread-safe initialization for the multi-turn session
    private val chatSession: Chat by lazy {
        model.startChat()
    }

    /**
     * Accepts a user string prompt, updates the active multi-turn session tracking,
     * and maps the response back into a nullable String suitable for your ChatPopup Composable.
     */
    suspend fun promptTest(prompt: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Submit message to the lazy-loaded Firebase session instance
                val response = chatSession.sendMessage(prompt)

                // Return the text back to the UI block
                response.text ?: "The model generated an empty response."
            } catch (e: Exception) {
                Log.e("GenAIViewModel", "Error inside chat session loop processing prompt", e)

                // Expose the raw message directly to the UI bubble to simplify tracking configurations
                "Error processing request: ${e.localizedMessage ?: "Please verify your Firebase AI settings."}"
            }
        }
    }
}