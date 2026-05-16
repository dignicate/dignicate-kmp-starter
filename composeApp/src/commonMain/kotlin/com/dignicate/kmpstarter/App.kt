package com.dignicate.kmpstarter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.dignicate.kmpstarter.core.AppConfig
import com.dignicate.kmpstarter.core.CommonBackHandler
import com.dignicate.kmpstarter.ui.feature.debug.DebugMenuTabScreen
import com.dignicate.kmpstarter.ui.feature.launch.LaunchScreen
import com.dignicate.kmpstarter.ui.feature.settings.SettingsScreen
import com.dignicate.kmpstarter.ui.navigation.MainNavigationContainer
import com.dignicate.kmpstarter.ui.navigation.MainTab
import com.dignicate.kmpstarter.viewmodel.feature.debug.DebugMenuViewModel
import com.dignicate.kmpstarter.viewmodel.feature.home.HomeViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class Screen {
    Launch, Home, Settings, DebugMenu
}

@Composable
fun App() {
    val appConfig: AppConfig = koinInject()
    val homeViewModel: HomeViewModel = koinViewModel()
    val debugMenuViewModel: DebugMenuViewModel = koinViewModel()
    var currentScreen by remember { mutableStateOf(Screen.Launch) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    CommonBackHandler(
        enabled = currentScreen == Screen.Settings || currentScreen == Screen.DebugMenu
    ) {
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
                version = appConfig.version,
                showsDebugMenu = appConfig.showsDebugMenu,
                onOpenSettings = { currentScreen = Screen.Settings },
                onOpenDebugMenu = { currentScreen = Screen.DebugMenu },
                homeViewModel = homeViewModel,
            )
            Screen.Settings -> SettingsScreen(
                onBack = { currentScreen = Screen.Home }
            )
            Screen.DebugMenu -> DebugMenuTabScreen(
                viewModel = debugMenuViewModel,
                onBack = { currentScreen = Screen.Home },
            )
        }
    }
}
