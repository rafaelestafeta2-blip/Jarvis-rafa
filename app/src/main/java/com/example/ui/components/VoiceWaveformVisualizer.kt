package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.StarkGold
import kotlin.random.Random

@Composable
fun VoiceWaveformVisualizer(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    volumeRms: Float = 0f,
    height: Dp = 48.dp,
    barCount: Int = 15
) {
    val active = isListening || isSpeaking
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barColor = if (isListening) StarkGold else ArcCyan

        for (i in 0 until barCount) {
            val animDuration = 300 + (i % 5) * 120
            val scaleFactor by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = if (active) (0.4f + ((i * 17) % 60) / 100f + (volumeRms / 15f).coerceIn(0f, 0.4f)) else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarHeight_$i"
            )

            val barHeight = if (active) height * scaleFactor else 4.dp

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .background(
                        color = if (active) barColor else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
