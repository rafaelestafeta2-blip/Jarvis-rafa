package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BottomHudNavBar
import com.example.ui.components.NavTab
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MetallicObsidian
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.RemindersViewModel
import com.example.ui.viewmodel.VoiceViewModel

@Composable
fun JarvisAppNav(
    jarvisViewModel: JarvisViewModel,
    remindersViewModel: RemindersViewModel,
    voiceViewModel: VoiceViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavTab.Home.route

    Scaffold(
        containerColor = MetallicObsidian,
        bottomBar = {
            BottomHudNavBar(
                currentRoute = currentRoute,
                onTabSelected = { tab ->
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(NavTab.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavTab.Home.route
            ) {
                composable(NavTab.Home.route) {
                    HomeScreen(
                        jarvisViewModel = jarvisViewModel,
                        remindersViewModel = remindersViewModel,
                        voiceViewModel = voiceViewModel,
                        onNavigateToChat = { navController.navigate(NavTab.Chat.route) },
                        onNavigateToProtocols = { navController.navigate(NavTab.Protocols.route) }
                    )
                }

                composable(NavTab.Chat.route) {
                    ChatScreen(
                        jarvisViewModel = jarvisViewModel,
                        voiceViewModel = voiceViewModel
                    )
                }

                composable(NavTab.Protocols.route) {
                    RemindersScreen(
                        remindersViewModel = remindersViewModel
                    )
                }

                composable(NavTab.Settings.route) {
                    SettingsScreen(
                        jarvisViewModel = jarvisViewModel,
                        voiceViewModel = voiceViewModel
                    )
                }
            }
        }
    }
}
