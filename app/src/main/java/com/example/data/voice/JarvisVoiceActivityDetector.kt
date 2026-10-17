package com.example.data.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.data.device.JarvisPowerManager
import com.example.data.device.JarvisPowerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class JarvisVoiceActivityDetector(
    private val context: Context? = null,
    private val onVoiceActivityDetected: () -> Unit
) {

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var powerManager: JarvisPowerManager? = context?.let { JarvisPowerManager(it) }

    val isVadActive = MutableStateFlow(false)
    val currentRmsDb = MutableStateFlow(0f)
    val currentPowerMode = MutableStateFlow(JarvisPowerMode.TURBO_CHARGING)

    // Speech threshold in RMS decibels
    private val voiceThresholdDb = 42.0f
    private var consecutiveSpeechFrames = 0
    private val requiredSpeechFrames = 3 // ~150ms of sustained speech

    @SuppressLint("MissingPermission")
    fun startVad() {
        if (isRecording) return

        try {
            if (bufferSize <= 0) {
                bufferSize = sampleRate * 2
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            isVadActive.value = true

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                while (isActive && isRecording) {
                    val mode = powerManager?.evaluatePowerMode() ?: JarvisPowerMode.TURBO_CHARGING
                    currentPowerMode.value = mode

                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        val rms = calculateRmsDb(buffer, readSize)
                        currentRmsDb.value = rms

                        if (rms >= voiceThresholdDb) {
                            consecutiveSpeechFrames++
                            if (consecutiveSpeechFrames >= requiredSpeechFrames) {
                                consecutiveSpeechFrames = 0
                                // Voice Activity Detected! Trigger JARVIS wake listener
                                scope.launch(Dispatchers.Main) {
                                    onVoiceActivityDetected()
                                }
                            }
                        } else {
                            consecutiveSpeechFrames = 0
                        }
                    }

                    // Battery-aware power management throttle delay
                    if (mode.samplingDelayMs > 0L) {
                        delay(mode.samplingDelayMs)
                    }
                }
            }
        } catch (e: Exception) {
            stopVad()
        }
    }

    fun stopVad() {
        isRecording = false
        isVadActive.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore release exceptions
        }
        audioRecord = null
    }

    private fun calculateRmsDb(buffer: ShortArray, readSize: Int): Float {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        val mean = sum / readSize
        val rms = sqrt(mean)
        return if (rms > 0) (20 * kotlin.math.log10(rms)).toFloat() else 0f
    }
}
