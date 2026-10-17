package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextMuted

sealed class NavTab(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    object Home : NavTab("home", "HOME", Icons.Default.Home, "nav_home")
    object Chat : NavTab("chat", "CHAT", Icons.Default.ChatBubble, "nav_chat")
    object Protocols : NavTab("protocols", "PROTOCOLS", Icons.Default.Assignment, "nav_protocols")
    object Settings : NavTab("settings", "SETTINGS", Icons.Default.Settings, "nav_settings")
}

@Composable
fun BottomHudNavBar(
    currentRoute: String,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(NavTab.Home, NavTab.Chat, NavTab.Protocols, NavTab.Settings)
    val shape = CutCornerShape(topStart = 16.dp, topEnd = 16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DeepSpaceSurface)
            .border(width = 1.dp, color = BorderCyan, shape = shape)
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                val tintColor = if (isSelected) ArcCyan else TextMuted

                IconButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.testTag(tab.tag)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = tintColor
                        )
                    }
                }
            }
        }
    }
}
