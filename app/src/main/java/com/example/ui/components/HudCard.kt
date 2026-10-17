package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.HolographicCardBackground

@Composable
fun HudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderCyan,
    backgroundColor: Color = HolographicCardBackground,
    cutCornerSize: Dp = 12.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = CutCornerShape(
        topStart = cutCornerSize,
        topEnd = 0.dp,
        bottomEnd = cutCornerSize,
        bottomStart = 0.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(contentPadding),
        content = content
    )
}
