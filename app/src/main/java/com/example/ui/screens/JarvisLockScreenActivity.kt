package com.example.ui.screens

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.JarvisDatabase
import com.example.data.device.DeviceController
import com.example.data.repository.JarvisRepository
import com.example.data.sound.JarvisSoundEffects
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.HudCard
import com.example.ui.components.IronManHudWaveform
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisTheme
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.JarvisViewModelFactory
import com.example.ui.viewmodel.VoiceViewModel

class JarvisLockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Lock Screen Overlay Flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val deviceController = DeviceController(applicationContext)
        val database = JarvisDatabase.getDatabase(applicationContext)
        val jarvisDao = database.jarvisDao()
        val jarvisRepository = JarvisRepository(jarvisDao, deviceController)

        val jarvisViewModel: JarvisViewModel by viewModels {
            JarvisViewModelFactory(jarvisRepository)
        }
        val voiceViewModel: VoiceViewModel by viewModels()

        setContent {
            JarvisTheme {
                LockScreenFloatingWidget(
                    jarvisViewModel = jarvisViewModel,
                    voiceViewModel = voiceViewModel,
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@Composable
fun LockScreenFloatingWidget(
    jarvisViewModel: JarvisViewModel,
    voiceViewModel: VoiceViewModel,
    onDismiss: () -> Unit
) {
    val isListening by voiceViewModel.isListening.collectAsState()
    val isSpeaking by voiceViewModel.isSpeaking.collectAsState()
    val spokenText by voiceViewModel.spokenText.collectAsState()
    val volumeRms by voiceViewModel.audioVolumeRms.collectAsState()
    val chatHistory by jarvisViewModel.chatHistory.collectAsState()

    val lastJarvisMessage = chatHistory.lastOrNull { it.sender == "JARVIS" }?.content ?: "JARVIS ativado na tela de bloqueio, Sir."

    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            jarvisViewModel.sendMessage(spokenText, isVoiceInput = true) { reply ->
                voiceViewModel.speak(reply)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        HudCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lock_screen_floating_widget"),
            borderColor = BorderCyan,
            backgroundColor = Color(0xEE0B1424),
            cutCornerSize = 16.dp,
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar with Dismiss Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ArcCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JARVIS LOCKSCREEN OVERLAY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcCyan
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Overlay", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arc Reactor Core
                ArcReactorCore(
                    size = 130.dp,
                    isActive = isListening || isSpeaking,
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

                // Canvas Waveform
                IronManHudWaveform(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    audioRmsDb = volumeRms,
                    height = 40.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Speech / Response Output Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(8.dp))
                        .background(Color(0xFF0F1F38))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isListening) "Ouvindo sua voz na tela de bloqueio..." else lastJarvisMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        color = if (isListening) StarkGold else TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Mic Trigger Button
                Row(
                    modifier = Modifier
                        .clip(CutCornerShape(10.dp))
                        .background(ArcCyan)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FALAR COM JARVIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
