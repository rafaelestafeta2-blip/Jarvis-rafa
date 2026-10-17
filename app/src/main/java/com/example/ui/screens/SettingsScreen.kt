package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.JarvisForegroundService
import com.example.ui.components.HudCard
import com.example.ui.components.HudHeader
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderGold
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.MetallicObsidian
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.VoiceViewModel

@Composable
fun SettingsScreen(
    jarvisViewModel: JarvisViewModel,
    voiceViewModel: VoiceViewModel
) {
    val context = LocalContext.current
    val apiKeyStatus by jarvisViewModel.apiKeyStatus.collectAsState()

    var pitchValue by remember { mutableFloatStateOf(0.88f) }
    var speedValue by remember { mutableFloatStateOf(0.98f) }
    var isBackgroundActive by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MetallicObsidian)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        HudHeader(title = "DIAGNOSTICS", subtitle = "JARVIS CORE SYSTEM CONFIGURATION")

        Spacer(modifier = Modifier.height(16.dp))

        // API Key Status HUD Card
        HudCard(
            borderColor = if (apiKeyStatus) BorderCyan else BorderGold,
            cutCornerSize = 12.dp
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (apiKeyStatus) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "API Key Status",
                        tint = if (apiKeyStatus) JarvisSuccessGreen else StarkGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (apiKeyStatus) "CHAVE API GEMINI DETECTADA" else "CHAVE API GEMINI AUSENTE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (apiKeyStatus)
                        "A comunicação neural com a API do Google Gemini está configurada e operacional via BuildConfig.GEMINI_API_KEY."
                    else
                        "Configure sua GEMINI_API_KEY no painel de Secrets do AI Studio para habilitar o raciocínio completo do JARVIS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification & Call Announcer Card
        HudCard(borderColor = BorderCyan, cutCornerSize = 12.dp) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call & Msg Announcer", tint = ArcCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANUNCIADOR DE MENSAGENS E CHAMADAS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O JARVIS lê em voz alta quem está ligando para você e o conteúdo de novas mensagens recebidas no WhatsApp, SMS ou outros aplicativos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = CutCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CONCEDER ACESSO A NOTIFICAÇÕES",
                        color = MetallicObsidian,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Foreground Service Card
        HudCard(borderColor = BorderGold, cutCornerSize = 12.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Background Service", tint = StarkGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODO DE SEGUNDO PLANO",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarkGold
                        )
                    }

                    Switch(
                        checked = isBackgroundActive,
                        onCheckedChange = { active ->
                            isBackgroundActive = active
                            if (active) {
                                JarvisForegroundService.startService(context)
                                voiceViewModel.speak("Serviço em segundo plano ativado. JARVIS permanecerá vigilante.")
                            } else {
                                JarvisForegroundService.stopService(context)
                                voiceViewModel.speak("Serviço em segundo plano desativado.")
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = StarkGold, checkedTrackColor = BorderGold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mantém o JARVIS ativo em segundo plano com uma notificação persistente do sistema Android, permitindo que os canais de voz fiquem prontos mesmo ao alternar ou minimizar o app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Engine Diagnostics Card
        HudCard(borderColor = BorderCyan, cutCornerSize = 12.dp) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = "Voice Settings", tint = ArcCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SINTETIZADOR DE VOZ MASCULINA JARVIS (HD)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TOM DA VOZ DE REATOR (PITCH): ${"%.2f".format(pitchValue)}x",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Slider(
                    value = pitchValue,
                    onValueChange = {
                        pitchValue = it
                        voiceViewModel.setSpeechPitch(it)
                    },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = ArcCyan, activeTrackColor = ArcCyan),
                    modifier = Modifier.testTag("voice_pitch_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "VELOCIDADE DE FALA DA IA (RATE): ${"%.2f".format(speedValue)}x",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Slider(
                    value = speedValue,
                    onValueChange = {
                        speedValue = it
                        voiceViewModel.setSpeechRate(it)
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = StarkGold, activeTrackColor = StarkGold),
                    modifier = Modifier.testTag("voice_speed_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { voiceViewModel.speak("Sistemas vocais de alta definição ajustados, Sir. Como posso servir a você hoje?") },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderCyan.copy(alpha = 0.3f)),
                        shape = CutCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_voice_button")
                    ) {
                        Text(text = "TESTAR AUDIO", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val ttsIntent = Intent("com.android.settings.TTS_SETTINGS").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(ttsIntent)
                            } catch (e: Exception) {
                                // Fallback to general settings
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarkGold),
                        shape = CutCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_tts_settings_button")
                    ) {
                        Text(text = "SELECIONAR VOZ HD", color = MetallicObsidian, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Maintenance Card
        HudCard(borderColor = BorderGold, cutCornerSize = 12.dp) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = StarkGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MANUTENÇÃO DE REGISTROS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarkGold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Limpar todos os registros da conversa neural salvos no banco de dados local Room.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { jarvisViewModel.clearChat() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert),
                    shape = CutCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_chat_history_button")
                ) {
                    Text(text = "LIMPAR HISTÓRICO DE CHAT", color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Spec Summary
        HudCard(borderColor = BorderCyan, cutCornerSize = 8.dp) {
            Column {
                Text(text = "STARK SYSTEM ARCHITECTURE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = ArcCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• AI ENGINE: Google Gemini 3.5-Flash (REST API)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                Text(text = "• UI LAYER: Jetpack Compose Material 3 Edge-to-Edge", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                Text(text = "• PERSISTENCE: Room SQLite Local Database", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                Text(text = "• VOICE SYNTHESIS: High Definition PT-BR Male Engine", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
