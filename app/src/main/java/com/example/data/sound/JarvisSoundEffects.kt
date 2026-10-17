package com.example.data.sound

import android.media.AudioManager
import android.media.ToneGenerator

object JarvisSoundEffects {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (e: Exception) {
        null
    }

    fun playActivationBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }

    fun playSuccessPulse() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    fun playAlertWarning() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200)
    }

    fun playClickSound() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
    }
}
