package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.db.ChatMessageEntity
import com.example.ui.components.HudCard
import com.example.ui.components.HudHeader
import com.example.ui.components.IronManHudWaveform
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderGold
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.HolographicCardBackground
import com.example.ui.theme.MetallicObsidian
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.VoiceViewModel

@Composable
fun ChatScreen(
    jarvisViewModel: JarvisViewModel,
    voiceViewModel: VoiceViewModel
) {
    val context = LocalContext.current
    val messages by jarvisViewModel.chatHistory.collectAsState()
    val uiState by jarvisViewModel.uiState.collectAsState()
    val isListening by voiceViewModel.isListening.collectAsState()
    val isSpeaking by voiceViewModel.isSpeaking.collectAsState()
    val spokenText by voiceViewModel.spokenText.collectAsState()
    val volumeRms by voiceViewModel.audioVolumeRms.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Photo Gallery Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            jarvisViewModel.analyzeImage(bitmap, inputText) { reply ->
                voiceViewModel.speak(reply)
            }
            inputText = ""
        }
    }

    // Camera Photo Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            jarvisViewModel.analyzeImage(it, inputText) { reply ->
                voiceViewModel.speak(reply)
            }
            inputText = ""
        }
    }

    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            inputText = spokenText
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MetallicObsidian)
            .padding(16.dp)
    ) {
        HudHeader(title = "J.A.R.V.I.S.", subtitle = "NEURAL VOICE & VISION CHANNEL")

        Spacer(modifier = Modifier.height(12.dp))

        // Iron Man HUD Canvas Waveform Component
        IronManHudWaveform(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioRmsDb = volumeRms,
            height = 56.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(
                    message = message,
                    onSpeakMessage = { voiceViewModel.speak(message.content) }
                )
            }

            if (uiState is UiState.Loading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ArcCyan,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S. está analisando os dados...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = ArcCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text & Voice & Vision Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Microphone Toggle
            IconButton(
                onClick = {
                    if (isListening) {
                        voiceViewModel.stopListening()
                    } else {
                        voiceViewModel.startListening()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isListening) StarkGold else DeepSpaceSurface)
                    .testTag("voice_input_toggle_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input Toggle",
                    tint = if (isListening) MetallicObsidian else ArcCyan
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Camera Vision Button
            IconButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DeepSpaceSurface)
                    .testTag("camera_vision_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera Vision",
                    tint = ArcCyan
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Photo Gallery Picker Button
            IconButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DeepSpaceSurface)
                    .testTag("gallery_picker_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery Picker",
                    tint = StarkGold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isListening) "Ouvindo..." else "Comando ou pergunta...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcCyan,
                    unfocusedBorderColor = BorderCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = CutCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        jarvisViewModel.sendMessage(textToSend, isVoiceInput = isListening) { reply ->
                            voiceViewModel.speak(reply)
                        }
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CutCornerShape(8.dp))
                    .background(ArcCyan)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = MetallicObsidian
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onSpeakMessage: () -> Unit
) {
    val isUser = message.sender == "USER"
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        HudCard(
            modifier = Modifier.fillMaxWidth(0.85f),
            borderColor = if (isUser) BorderGold else BorderCyan,
            backgroundColor = if (isUser) Color(0xFF182212) else HolographicCardBackground,
            cutCornerSize = 10.dp,
            contentPadding = 12.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU [SIR]" else "J.A.R.V.I.S.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) StarkGold else ArcCyan
                    )

                    IconButton(
                        onClick = onSpeakMessage,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak Text",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}
