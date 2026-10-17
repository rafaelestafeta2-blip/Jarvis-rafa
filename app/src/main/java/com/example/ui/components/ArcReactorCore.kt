package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.StarkGold

@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    isActive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransition")

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationOuter"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationInner"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_core")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f * 0.85f

            // Outer Glow Circle
            drawCircle(
                color = ArcCyan.copy(alpha = 0.15f * pulseGlow),
                radius = radius * 1.15f,
                center = center
            )

            // Outer Metallic Ring
            drawCircle(
                color = ArcCyan,
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Rotating Outer Segments
            rotate(rotationOuter, pivot = center) {
                for (i in 0 until 6) {
                    val angle = i * 60f
                    val arcPath = Path().apply {
                        addArc(
                            oval = androidx.compose.ui.geometry.Rect(
                                center.x - radius * 0.9f,
                                center.y - radius * 0.9f,
                                center.x + radius * 0.9f,
                                center.y + radius * 0.9f
                            ),
                            startAngleDegrees = angle,
                            sweepAngleDegrees = 30f
                        )
                    }
                    drawPath(
                        path = arcPath,
                        color = StarkGold,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }

            // Rotating Inner Ring & Triangle Core
            rotate(rotationInner, pivot = center) {
                val triRadius = radius * 0.55f
                val triPath = Path().apply {
                    moveTo(center.x, center.y - triRadius)
                    lineTo(center.x + triRadius * 0.866f, center.y + triRadius * 0.5f)
                    lineTo(center.x - triRadius * 0.866f, center.y + triRadius * 0.5f)
                    close()
                }
                drawPath(
                    path = triPath,
                    color = ArcCyan.copy(alpha = 0.8f * pulseGlow),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Inner Glowing Energy Core
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * pulseGlow),
                radius = radius * 0.25f,
                center = center
            )
            drawCircle(
                color = ArcCyan,
                radius = radius * 0.18f,
                center = center
            )
        }
    }
}
