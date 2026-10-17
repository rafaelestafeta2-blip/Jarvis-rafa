package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener, RecognitionListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var speechRecognizer: SpeechRecognizer? = null
    private var vadDetector: JarvisVoiceActivityDetector? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    val isListening = MutableStateFlow(false)
    val isSpeaking = MutableStateFlow(false)
    val isContinuousMode = MutableStateFlow(false)
    val spokenTextResult = MutableStateFlow("")
    val audioVolumeRms = MutableStateFlow(0f)
    val voiceError = MutableStateFlow<String?>(null)

    var speechPitch: Float = 0.92f // Deeper, sophisticated Jarvis tone
    var speechRate: Float = 1.02f  // Fluent, natural Brazilian cadence

    init {
        initSpeechRecognizer()
        setupTtsListener()
        setupVad()
    }

    private fun setupVad() {
        vadDetector = JarvisVoiceActivityDetector {
            if (!isListening.value && !isSpeaking.value) {
                startListening()
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceManager)
            }
        } else {
            voiceError.value = "Reconhecimento de voz não disponível neste dispositivo."
        }
    }

    private fun setupTtsListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking.value = true
                vadDetector?.stopVad()
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking.value = false
                if (isContinuousMode.value) {
                    scope.launch {
                        delay(600)
                        startListening()
                    }
                } else {
                    vadDetector?.startVad()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking.value = false
                if (!isContinuousMode.value) {
                    vadDetector?.startVad()
                }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ptBrLocale = Locale("pt", "BR")
            tts?.language = ptBrLocale

            // Scan available voices for high-quality Portuguese male voice
            try {
                val availableVoices = tts?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val bestJarvisVoice = availableVoices.firstOrNull { v ->
                        val name = v.name.lowercase()
                        v.locale == ptBrLocale && (
                                name.contains("male") ||
                                name.contains("masculino") ||
                                name.contains("jef") ||
                                name.contains("jab") ||
                                name.contains("ptm") ||
                                name.contains("local")
                        ) && !v.isNetworkConnectionRequired
                    } ?: availableVoices.firstOrNull { v -> v.locale == ptBrLocale && !v.isNetworkConnectionRequired }

                    bestJarvisVoice?.let { voice ->
                        tts?.voice = voice
                    }
                }
            } catch (e: Exception) {
                // Fallback to default PT-BR
            }

            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechRate)
        }
    }

    fun speak(text: String) {
        tts?.let { engine ->
            engine.setPitch(speechPitch)
            engine.setSpeechRate(speechRate)
            isSpeaking.value = true
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_UTTERANCE_ID")
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_UTTERANCE_ID")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking.value = false
    }

    fun startListening() {
        vadDetector?.stopVad()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        stopSpeaking()
        spokenTextResult.value = ""
        voiceError.value = null

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
            isListening.value = true
        } catch (e: Exception) {
            isListening.value = false
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening.value = false
    }

    fun toggleContinuousHandsFreeMode(enable: Boolean) {
        isContinuousMode.value = enable
        if (enable) {
            vadDetector?.startVad()
            startListening()
        } else {
            vadDetector?.stopVad()
            stopListening()
        }
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) { isListening.value = true }
    override fun onBeginningOfSpeech() { isListening.value = true }
    override fun onRmsChanged(rmsdB: Float) { audioVolumeRms.value = rmsdB }
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { isListening.value = false }

    override fun onError(error: Int) {
        isListening.value = false
        if (isContinuousMode.value && !isSpeaking.value) {
            scope.launch {
                delay(1200)
                startListening()
            }
        } else {
            vadDetector?.startVad()
        }
    }

    override fun onResults(results: Bundle?) {
        isListening.value = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            spokenTextResult.value = matches[0]
        } else if (isContinuousMode.value && !isSpeaking.value) {
            scope.launch {
                delay(800)
                startListening()
            }
        } else {
            vadDetector?.startVad()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            spokenTextResult.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        vadDetector?.stopVad()
        speechRecognizer?.destroy()
    }
}
