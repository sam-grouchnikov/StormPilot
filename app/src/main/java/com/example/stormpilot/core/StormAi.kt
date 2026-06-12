package com.example.stormpilot.core

import androidx.lifecycle.ViewModel
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class StormAiRepository @Inject constructor(
    private val stormPilotApi: StormPilotApi,
) {
    suspend fun submitRequest(input: String, location: String): Result<StormAiResponse> =
        runCatching {
            parseStormAiResponse(stormPilotApi.submitAssistantRequest(input, location))
        }
}

@HiltViewModel
class StormAiViewModel @Inject constructor(
    private val repository: StormAiRepository,
) : ViewModel() {
    suspend fun submitRequest(input: String, location: String): Result<StormAiResponse> =
        repository.submitRequest(input, location)
}
