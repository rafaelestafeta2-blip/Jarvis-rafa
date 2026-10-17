package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.JarvisPersona
import com.example.data.db.ChatMessageEntity
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val errorMessage: String) : UiState()
}

class JarvisViewModel(private val repository: JarvisRepository) : ViewModel() {

    val chatHistory: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .let { flow ->
            val stateFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
            viewModelScope.launch {
                flow.collect { messages ->
                    if (messages.isEmpty()) {
                        repository.saveMessage("JARVIS", JarvisPersona.getInitialGreeting())
                    } else {
                        stateFlow.value = messages
                    }
                }
            }
            stateFlow.asStateFlow()
        }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _apiKeyStatus = MutableStateFlow<Boolean>(false)
    val apiKeyStatus: StateFlow<Boolean> = _apiKeyStatus.asStateFlow()

    init {
        checkApiKeyStatus()
    }

    fun checkApiKeyStatus() {
        val key = BuildConfig.GEMINI_API_KEY
        _apiKeyStatus.value = key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    fun sendMessage(userText: String, isVoiceInput: Boolean = false, onReplyReceived: ((String) -> Unit)? = null) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.saveMessage("USER", userText, isVoiceInput)

            val currentHistory = chatHistory.value
            val result = repository.askJarvis(userText, currentHistory)

            result.onSuccess { reply ->
                repository.saveMessage("JARVIS", reply, isVoice = false)
                _uiState.value = UiState.Success(reply)
                onReplyReceived?.invoke(reply)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Erro de comunicação neural."
                _uiState.value = UiState.Error(errorMsg)
                repository.saveMessage("JARVIS", "Sir, encontrei uma anomalia: $errorMsg")
            }
        }
    }

    fun analyzeImage(bitmap: Bitmap, promptText: String, onReplyReceived: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val prompt = promptText.ifBlank { "Analise visual da câmera capturada." }
            repository.saveMessage("USER", "[IMAGEM ENVIADA PARA ANÁLISE]: $prompt")

            val result = repository.analyzeVisionImage(bitmap, prompt)

            result.onSuccess { reply ->
                repository.saveMessage("JARVIS", reply, isVoice = false)
                _uiState.value = UiState.Success(reply)
                onReplyReceived?.invoke(reply)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Erro de processamento óptico."
                _uiState.value = UiState.Error(errorMsg)
                repository.saveMessage("JARVIS", "Sir, falha no sensor óptico: $errorMsg")
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.saveMessage("JARVIS", "Histórico de conversas limpo, Sir. Todos os registros foram arquivados.")
        }
    }
}

class JarvisViewModelFactory(private val repository: JarvisRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JarvisViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}
