package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.device.DeviceController
import com.example.data.sound.JarvisSoundEffects
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.HudCard
import com.example.ui.components.HudHeader
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderGold
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.MetallicObsidian
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.RemindersViewModel
import com.example.ui.viewmodel.VoiceViewModel

@Composable
fun HomeScreen(
    jarvisViewModel: JarvisViewModel,
    remindersViewModel: RemindersViewModel,
    voiceViewModel: VoiceViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProtocols: () -> Unit
) {
    val context = LocalContext.current
    val deviceController = DeviceController(context)

    val isListening by voiceViewModel.isListening.collectAsState()
    val isSpeaking by voiceViewModel.isSpeaking.collectAsState()
    val isContinuousMode by voiceViewModel.isContinuousMode.collectAsState()
    val reminders by remindersViewModel.reminders.collectAsState()
    val apiKeyConfigured by jarvisViewModel.apiKeyStatus.collectAsState()

    val pendingReminders = reminders.filter { !it.isCompleted }
    val batteryPct = deviceController.getBatteryPercentage()
    val netStatus = deviceController.getNetworkStatus()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MetallicObsidian)
            .padding(16.dp)
    ) {
        HudHeader(title = "J.A.R.V.I.S. v2.5 ULTIMATE", subtitle = "STARK TACTICAL & DEVICE CONTROL HUD", isOnline = apiKeyConfigured)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arc Reactor Central Visualizer Block
            item {
                HudCard(
                    modifier = Modifier.testTag("arc_reactor_card"),
                    borderColor = BorderCyan,
                    cutCornerSize = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isListening) "LISTENING TO COMMAND..." else if (isSpeaking) "J.A.R.V.I.S. SPEAKING..." else if (isContinuousMode) "HANDS-FREE ALEXA MODE ACTIVE" else "SYSTEM CORE ACTIVE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (isListening || isContinuousMode) StarkGold else ArcCyan,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ArcReactorCore(
                            size = 150.dp,
                            isActive = isListening || isSpeaking || isContinuousMode,
                            onClick = {
                                JarvisSoundEffects.playActivationBeep()
                                if (!isListening) {
                                    voiceViewModel.startListening()
                                } else {
                                    voiceViewModel.stopListening()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CutCornerShape(10.dp))
                                    .background(BorderCyan.copy(alpha = 0.2f))
                                    .clickable {
                                        JarvisSoundEffects.playClickSound()
                                        onNavigateToChat()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("trigger_chat_button")
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Command", tint = ArcCyan)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VOICE CHAT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CutCornerShape(10.dp))
                                    .background(BorderGold.copy(alpha = 0.2f))
                                    .clickable {
                                        JarvisSoundEffects.playSuccessPulse()
                                        val statusStr = deviceController.toggleFlashlight(!deviceController.isFlashlightActive())
                                        voiceViewModel.speak(statusStr)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("toggle_flashlight_button")
                            ) {
                                Icon(imageVector = Icons.Default.FlashlightOn, contentDescription = "Flashlight", tint = StarkGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LANTERNA",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Continuous Hands-Free Alexa Mode Switch Card
            item {
                HudCard(borderColor = if (isContinuousMode) BorderGold else BorderCyan, cutCornerSize = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Hands-Free", tint = if (isContinuousMode) StarkGold else ArcCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MODO MÃOS LIVRES (ESTILO ALEXA)",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isContinuousMode) StarkGold else ArcCyan
                                )
                                Text(
                                    text = if (isContinuousMode) "Escuta contínua ativa - Fale sem pressionar botões" else "Toque no botão para ativar escuta automática contínua",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isContinuousMode,
                            onCheckedChange = { active ->
                                JarvisSoundEffects.playActivationBeep()
                                voiceViewModel.toggleContinuousHandsFreeMode(active)
                                if (active) {
                                    voiceViewModel.speak("Modo mãos livres ativado, Sir. Estou ouvindo continuamente.")
                                } else {
                                    voiceViewModel.speak("Modo mãos livres desativado.")
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StarkGold, checkedTrackColor = BorderGold),
                            modifier = Modifier.testTag("hands_free_switch")
                        )
                    }
                }
            }

            // Real Device Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BatteryChargingFull,
                        label = "BATERIA",
                        value = "$batteryPct%",
                        accentColor = ArcCyan
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SdStorage,
                        label = "MEMÓRIA",
                        value = "DISPONÍVEL",
                        accentColor = StarkGold
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Psychology,
                        label = "SISTEMA",
                        value = netStatus,
                        accentColor = ArcCyan
                    )
                }
            }

            // Stark Special Macros Hub
            item {
                HudCard(borderColor = JarvisRedAlert, cutCornerSize = 12.dp) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Protocol Macros", tint = JarvisRedAlert)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STARK PROTOCOL MACROS (PREMIUM)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = JarvisRedAlert,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        QuickCommandChip("Protocolo House Party (Festa de Reatores)") {
                            JarvisSoundEffects.playAlertWarning()
                            deviceController.toggleFlashlight(true)
                            voiceViewModel.speak("Protocolo House Party ativado! Todos os reatores operando em capacidade máxima, Sir.")
                        }

                        QuickCommandChip("Modo Oficina / Silencioso") {
                            JarvisSoundEffects.playClickSound()
                            deviceController.setVolumePercentage(0)
                            voiceViewModel.speak("Modo Oficina ativado. Volume do som ajustado para o mínimo.")
                        }

                        QuickCommandChip("Diagnóstico completo do dispositivo") {
                            JarvisSoundEffects.playSuccessPulse()
                            jarvisViewModel.sendMessage("Diagnóstico completo")
                            onNavigateToChat()
                        }
                    }
                }
            }

            // Active Reminders Summary
            item {
                HudCard(borderColor = BorderCyan, cutCornerSize = 12.dp) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PENDING PROTOCOLS (${pendingReminders.size})",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = ArcCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "VIEW ALL",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = StarkGold,
                                modifier = Modifier.clickable { onNavigateToProtocols() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (pendingReminders.isEmpty()) {
                            Text(
                                text = "No active protocols pending, Sir.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            pendingReminders.take(2).forEach { reminder ->
                                Text(
                                    text = "• ${reminder.title} [${reminder.priority}]",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    HudCard(
        modifier = modifier,
        borderColor = accentColor.copy(alpha = 0.5f),
        cutCornerSize = 8.dp,
        contentPadding = 10.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextSecondary)
            Text(text = value, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickCommandChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(CutCornerShape(6.dp))
            .background(BorderCyan.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = "> $text", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary)
    }
}
