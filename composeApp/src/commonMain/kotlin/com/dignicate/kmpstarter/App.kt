package com.dignicate.kmpstarter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.dignicate.kmpstarter.ui.feature.launch.LaunchScreen
import com.dignicate.kmpstarter.ui.feature.settings.SettingsScreen
import com.dignicate.kmpstarter.ui.navigation.MainNavigationContainer
import com.dignicate.kmpstarter.ui.navigation.MainTab
import com.dignicate.kmpstarter.core.CommonBackHandler
import com.dignicate.kmpstarter.core.getAppVersion
import com.dignicate.kmpstarter.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

enum class Screen {
    Launch, Home, Settings
}

@Composable
fun App() {
    val appVersion = remember { getAppVersion() }
    val homeViewModel: HomeViewModel = koinViewModel()
    var currentScreen by remember { mutableStateOf(Screen.Launch) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    CommonBackHandler(enabled = currentScreen == Screen.Settings) {
        currentScreen = Screen.Home
    }

    MaterialTheme {
        when (currentScreen) {
            Screen.Launch -> LaunchScreen(
                onFinished = {
                    currentScreen = Screen.Home
                    selectedTab = MainTab.HOME
                }
            )
            Screen.Home -> MainNavigationContainer(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                version = appVersion,
                onOpenSettings = { currentScreen = Screen.Settings },
                homeViewModel = homeViewModel,
            )
            Screen.Settings -> SettingsScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}
