package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.JarvisPersona
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.ChatMessageEntity
import com.example.data.db.JarvisDao
import com.example.data.db.UserMemoryEntity
import com.example.data.device.DeviceController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class JarvisRepository(
    private val dao: JarvisDao,
    private val deviceController: DeviceController? = null
) {

    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val userMemories: Flow<List<UserMemoryEntity>> = dao.getAllMemories()

    suspend fun saveMessage(sender: String, content: String, isVoice: Boolean = false) {
        dao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                content = content,
                isVoice = isVoice
            )
        )
    }

    suspend fun saveUserMemory(category: String, memoryText: String) {
        dao.insertMemory(
            UserMemoryEntity(
                keyCategory = category,
                memoryContent = memoryText
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearChatHistory()
    }

    suspend fun processSystemIntent(userPrompt: String): String? {
        val lower = userPrompt.lowercase().trim()

        // Notification Summary Query Intent
        if (lower.contains("notificação") || lower.contains("notificações") || lower.contains("mensagens recebidas")) {
            val logs = dao.getRecentNotificationLogsList()
            if (logs.isEmpty()) {
                return "Nenhuma notificação recente registrada nos logs do sistema, Sir."
            }
            val summary = logs.take(5).joinToString("\n") { "• [${it.appLabel}] ${it.title}: ${it.contentText}" }
            return "Resumo das últimas notificações interceptadas, Sir:\n$summary"
        }

        // Memory Learning Intent Detection ("Lembre-se que...", "Meu nome é...", "Eu gosto de...")
        if (lower.startsWith("lembre-se que ") || lower.startsWith("lembre-se de ") || lower.startsWith("meu nome é ") || lower.startsWith("eu gosto de ")) {
            saveUserMemory("FACT", userPrompt)
            return "Compreendido, Sir. Gravando esta informação em meu núcleo de memória de longo prazo."
        }

        // App Launch Intent Detection ("abra o whatsapp", "abre o zap", "abrir whassap", "abrir youtube", "iniciar spotify")
        val isAppLaunch = lower.startsWith("abra") || lower.startsWith("abre") || lower.startsWith("abrir") ||
                lower.startsWith("iniciar") || lower.startsWith("lançar") || lower.startsWith("execute") ||
                lower.contains("abra o ") || lower.contains("abra a ") || lower.contains("abre o ") || lower.contains("abre a ") ||
                lower.contains("abrir o ") || lower.contains("abrir a ") ||
                lower.contains("whatsapp") || lower.contains("whassap") || lower.contains("zap") || lower.contains("wats") || lower.contains("whats")

        if (isAppLaunch) {
            val targetApp = lower
                .replace("pode ", "")
                .replace("por favor ", "")
                .replace("abrir o ", "")
                .replace("abrir a ", "")
                .replace("abrir ", "")
                .replace("abra o ", "")
                .replace("abra a ", "")
                .replace("abra ", "")
                .replace("abre o ", "")
                .replace("abre a ", "")
                .replace("abre ", "")
                .replace("iniciar o ", "")
                .replace("iniciar a ", "")
                .replace("iniciar ", "")
                .replace("lançar ", "")
                .replace("execute ", "")
                .replace("para mim", "")
                .replace("pra mim", "")
                .trim()

            val queryToUse = if (targetApp.contains("whatsapp") || targetApp.contains("whassap") || targetApp.contains("zap") || targetApp.contains("whats") || targetApp.contains("wats")) {
                "whatsapp"
            } else if (targetApp.isNotBlank()) {
                targetApp
            } else {
                lower
            }

            return deviceController?.openApplication(queryToUse)
        }

        return when {
            lower.contains("lanterna") && (lower.contains("liga") || lower.contains("ativa") || lower.contains("acenda")) -> {
                deviceController?.toggleFlashlight(true)
            }
            lower.contains("lanterna") && (lower.contains("desliga") || lower.contains("desativa") || lower.contains("apague")) -> {
                deviceController?.toggleFlashlight(false)
            }
            lower.contains("bateria") -> {
                val pct = deviceController?.getBatteryPercentage() ?: 100
                val charging = if (deviceController?.isBatteryCharging() == true) "e está carregando" else "em uso"
                "Sua bateria está em $pct% $charging, Sir."
            }
            lower.contains("diagnóstico") || lower.contains("status do sistema") || lower.contains("relatório") -> {
                deviceController?.generateFullDiagnosticReport()
            }
            lower.contains("armazenamento") || lower.contains("memória") || lower.contains("espaço") -> {
                val storageStr = deviceController?.getFormattedFreeStorage() ?: "Espaço disponível OK."
                "Diagnóstico de memória: $storageStr, Sir."
            }
            else -> null
        }
    }

    suspend fun askJarvis(userPrompt: String, history: List<ChatMessageEntity>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Check if user prompt is a local device system command, app launch or memory store
                val localActionReply = processSystemIntent(userPrompt)
                if (localActionReply != null) {
                    return@withContext Result.success(localActionReply)
                }

                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    return@withContext Result.failure(
                        IllegalStateException("API Key do Gemini não configurada no Secrets panel.")
                    )
                }

                // Fetch long term memories to inject into system prompt
                val savedMemories = dao.getMemoriesList()
                val memoryContext = if (savedMemories.isNotEmpty()) {
                    "\n\n[MEMÓRIAS APRENDIDAS SOBRE O USUÁRIO]:\n" + savedMemories.joinToString("\n") { "• ${it.memoryContent}" }
                } else ""

                val systemPromptWithMemories = JarvisPersona.SYSTEM_PROMPT + memoryContext

                // Short-Term Memory: Fetch exact last 10 conversational exchanges from Room DB
                val shortTermHistory = dao.getRecentShortTermMessages(limit = 10)
                val recentTurnContents = shortTermHistory.map { msg ->
                    Content(
                        role = if (msg.sender == "USER") "user" else "model",
                        parts = listOf(Part(text = msg.content))
                    )
                }.toMutableList()

                recentTurnContents.add(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = userPrompt))
                    )
                )

                val request = GenerateContentRequest(
                    contents = recentTurnContents,
                    systemInstruction = Content(
                        parts = listOf(Part(text = systemPromptWithMemories))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        maxOutputTokens = 800
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!replyText.isNullOrBlank()) {
                    Result.success(replyText.trim())
                } else {
                    Result.failure(Exception("J.A.R.V.I.S. não retornou resposta no canal neural."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun analyzeVisionImage(bitmap: Bitmap, prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    return@withContext Result.failure(
                        IllegalStateException("API Key do Gemini não configurada no Secrets panel.")
                    )
                }

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
                val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            role = "user",
                            parts = listOf(
                                Part(text = prompt.ifBlank { "Analise esta imagem em detalhes como JARVIS." }),
                                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                            )
                        )
                    ),
                    systemInstruction = Content(
                        parts = listOf(Part(text = JarvisPersona.SYSTEM_PROMPT))
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!replyText.isNullOrBlank()) {
                    Result.success(replyText.trim())
                } else {
                    Result.failure(Exception("J.A.R.V.I.S. não conseguiu processar os dados ópticos."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
