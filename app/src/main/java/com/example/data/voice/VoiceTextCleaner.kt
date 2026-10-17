package com.example.data.voice

import java.util.regex.Pattern

/**
 * Utilitário responsável pela limpeza e formatação de texto para o sintetizador de voz (TTS).
 * Remove artefatos de Markdown, emojis e caracteres especiais que causam gagueira, travamentos
 * ou pronúncias mecânicas indesejadas no sintetizador de voz do JARVIS.
 */
object VoiceTextCleaner {

    private val EMOJI_REGEX = Pattern.compile(
        "[\\p{So}\\p{Cn}\\uFE00-\\uFE0F\\uE000-\\uF8FF]|\\uD83C[\\uDF00-\\uDFFF]|\\uD83D[\\uDC00-\\uDE4F]|\\uD83E[\\uDD00-\\uDDFF]"
    )
    private val MARKDOWN_LINKS = Pattern.compile("\\[([^\\]]+)\\]\\([^\\)]+\\)")
    private val MARKDOWN_FORMATTING = Pattern.compile("[*_~`#>\\[\\]|]")
    private val BULLET_POINTS = Pattern.compile("(?m)^\\s*[-*•]\\s*")
    private val MULTIPLE_SPACES = Pattern.compile("\\s+")
    private val MULTIPLE_PUNCTUATION = Pattern.compile("([.!?])\\1+")

    /**
     * Limpa o texto da IA (Gemini) para torná-lo fluido, natural e com cadência cinematográfica.
     */
    fun cleanForSpeech(input: String): String {
        if (input.isBlank()) return ""

        var text = input

        // Substitui links no formato [texto](url) apenas pelo texto
        text = MARKDOWN_LINKS.matcher(text).replaceAll("$1")

        // Remove marcadores de lista no início de linhas
        text = BULLET_POINTS.matcher(text).replaceAll("")

        // Remove caracteres de formatação do Markdown (negrito, itálico, cabeçalhos, código)
        text = MARKDOWN_FORMATTING.matcher(text).replaceAll("")

        // Remove emojis que causam leitura literal ("rosto sorridente...")
        text = EMOJI_REGEX.matcher(text).replaceAll("")

        // Normaliza abreviações comuns para pronúncia elegante em português
        text = text
            .replace(Regex("(?i)\\bSr\\.\\b"), "Senhor")
            .replace(Regex("(?i)\\bSra\\.\\b"), "Senhora")
            .replace(Regex("(?i)\\bDr\\.\\b"), "Doutor")
            .replace(Regex("(?i)\\bmin\\b"), "minutos")
            .replace(Regex("(?i)\\bseg\\b"), "segundos")
            .replace(Regex("(?i)\\bh\\b"), "horas")
            .replace(Regex("(?i)\\bkm/h\\b"), "quilômetros por hora")
            .replace(Regex("(?i)\\b°C\\b"), "graus Celsius")
            .replace(Regex("(?i)\\b%\\b"), " por cento")
            .replace("JARVIS", "Járvis")
            .replace("J.A.R.V.I.S.", "Járvis")

        // Normaliza pontuações repetidas (... -> .) e espaçamentos múltiplos
        text = MULTIPLE_PUNCTUATION.matcher(text).replaceAll("$1")
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ").trim()

        return text
    }

    /**
     * Divide textos longos em sentenças naturais para evitar buffering ou corte no sintetizador.
     */
    fun splitIntoSentences(text: String, maxChunkSize: Int = 220): List<String> {
        val cleaned = cleanForSpeech(text)
        if (cleaned.length <= maxChunkSize) return listOf(cleaned)

        val sentences = cleaned.split(Regex("(?<=[.!?])\\s+"))
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()

        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > maxChunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString().trim())
                currentChunk = StringBuilder()
            }
            if (currentChunk.isNotEmpty()) currentChunk.append(" ")
            currentChunk.append(sentence)
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return chunks
    }
}
