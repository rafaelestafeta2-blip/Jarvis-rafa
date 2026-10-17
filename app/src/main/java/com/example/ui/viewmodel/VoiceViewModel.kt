package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.voice.VoiceManager
import kotlinx.coroutines.flow.StateFlow

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val voiceManager = VoiceManager(application)

    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val isContinuousMode: StateFlow<Boolean> = voiceManager.isContinuousMode
    val spokenText: StateFlow<String> = voiceManager.spokenTextResult
    val audioVolumeRms: StateFlow<Float> = voiceManager.audioVolumeRms
    val error: StateFlow<String?> = voiceManager.voiceError

    fun speak(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun toggleContinuousHandsFreeMode(enable: Boolean) {
        voiceManager.toggleContinuousHandsFreeMode(enable)
    }

    fun setSpeechPitch(pitch: Float) {
        voiceManager.speechPitch = pitch
    }

    fun setSpeechRate(rate: Float) {
        voiceManager.speechRate = rate
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
