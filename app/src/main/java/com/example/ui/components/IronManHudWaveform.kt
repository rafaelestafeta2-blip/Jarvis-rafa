package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.StarkGold
import kotlin.math.sin

@Composable
fun IronManHudWaveform(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    audioRmsDb: Float = 0f,
    height: Dp = 64.dp
) {
    val active = isListening || isSpeaking
    val infiniteTransition = rememberInfiniteTransition(label = "HudWaveformTransition")

    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseShift"
    )

    val rotationHud by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationHud"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("iron_man_hud_waveform"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val width = size.width
            val heightPx = size.height
            val centerY = heightPx / 2f

            // Normalize audio RMS amplitude to a scale factor (10dB - 60dB range)
            val normalizedAmp = if (active) {
                ((audioRmsDb - 10f) / 45f).coerceIn(0.15f, 1.2f)
            } else {
                0.05f
            }

            val waveColor = if (isListening) StarkGold else ArcCyan

            // 1. Draw Background Sci-Fi HUD Grid Lines
            drawLine(
                color = ArcCyan.copy(alpha = 0.2f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // 2. Draw Primary Sine Wave Path
            val primaryWavePath = Path()
            primaryWavePath.moveTo(0f, centerY)

            val wavelength = width / 2f
            val maxAmp = (heightPx / 2.2f) * normalizedAmp

            var x = 0f
            while (x <= width) {
                val relativeX = x / wavelength
                val y = centerY + sin(relativeX * 2 * Math.PI + phaseShift).toFloat() * maxAmp
                primaryWavePath.lineTo(x, y)
                x += 4f
            }

            drawPath(
                path = primaryWavePath,
                color = waveColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 3. Draw Secondary Inverse Harmonic Wave Path
            val secondaryWavePath = Path()
            secondaryWavePath.moveTo(0f, centerY)

            x = 0f
            while (x <= width) {
                val relativeX = x / (wavelength * 0.7f)
                val y = centerY - sin(relativeX * 2 * Math.PI - phaseShift * 1.5f).toFloat() * (maxAmp * 0.6f)
                secondaryWavePath.lineTo(x, y)
                x += 4f
            }

            drawPath(
                path = secondaryWavePath,
                color = ArcCyan.copy(alpha = 0.6f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 4. Center Glowing Circular Scope Target when Active
            if (active) {
                val centerOffset = Offset(width / 2f, centerY)
                rotate(rotationHud, pivot = centerOffset) {
                    drawCircle(
                        color = waveColor.copy(alpha = 0.3f),
                        radius = 24.dp.toPx() * normalizedAmp,
                        center = centerOffset,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    for (i in 0 until 4) {
                        val angle = i * 90f
                        val rad = Math.toRadians(angle.toDouble())
                        val r1 = 18.dp.toPx() * normalizedAmp
                        val r2 = 28.dp.toPx() * normalizedAmp
                        val p1 = Offset(centerOffset.x + (r1 * Math.cos(rad)).toFloat(), centerOffset.y + (r1 * Math.sin(rad)).toFloat())
                        val p2 = Offset(centerOffset.x + (r2 * Math.cos(rad)).toFloat(), centerOffset.y + (r2 * Math.sin(rad)).toFloat())
                        drawLine(color = waveColor, start = p1, end = p2, strokeWidth = 2.dp.toPx())
                    }
                }
            }
        }
    }
}
