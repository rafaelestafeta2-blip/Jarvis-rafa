package com.example.data.voice

import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

/**
 * Especialista na seleção e calibração da voz do JARVIS no padrão da dublagem brasileira.
 */
object VoiceSelector {

    // Tom mais encorpado e grave (estilo Eduardo Borgerth / mordomo britânico refinado)
    const val DEFAULT_JARVIS_PITCH = 0.88f
    // Cadência fluida, calma e firme
    const val DEFAULT_JARVIS_RATE = 0.98f

    fun configureAudioAttributes(tts: TextToSpeech) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        tts.setAudioAttributes(audioAttributes)
    }

    fun findBestJarvisVoice(tts: TextToSpeech): Voice? {
        val voices = try {
            tts.voices
        } catch (_: Exception) {
            null
        } ?: return null

        val ptBrLocale = Locale("pt", "BR")

        // Lista priorizada de vozes masculinas brasileiras de alta definição (Google Speech Services)
        return voices
            .filter { it.locale.language == "pt" }
            .maxByOrNull { voice ->
                var score = 0
                val name = voice.name.lowercase()

                if (voice.locale == ptBrLocale || voice.locale.country.equals("BR", ignoreCase = true)) {
                    score += 60
                }

                // Preferência por vozes masculinas
                if (name.contains("male") || name.contains("masculino") || name.contains("homem")) {
                    score += 50
                }

                // Pacotes neurais conhecidos do Google TTS em pt-BR
                if (name.contains("pt-br-x-afs") || name.contains("pt-br-x-jab") || name.contains("pt-br-x-jef")) {
                    score += 40
                }

                if (name.contains("ptm") || name.contains("ptd") || name.contains("ptb")) {
                    score += 30
                }

                if (voice.quality >= Voice.QUALITY_HIGH) {
                    score += 25
                } else if (voice.quality >= Voice.QUALITY_NORMAL) {
                    score += 10
                }

                if (!voice.isNetworkConnectionRequired) {
                    score += 15
                }

                // Penaliza vozes femininas para preservar a identidade do JARVIS
                if (name.contains("female") || name.contains("feminino") || name.contains("mulher")) {
                    score -= 80
                }

                score
            }
    }
}
