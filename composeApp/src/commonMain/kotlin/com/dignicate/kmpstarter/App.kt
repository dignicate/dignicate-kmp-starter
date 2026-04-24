package com.dignicate.kmpstarter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.dignicate.kmpstarter.core.AppConfig
import com.dignicate.kmpstarter.core.Coordinator
import com.dignicate.kmpstarter.core.LocalAppConfig
import com.dignicate.kmpstarter.core.LocalCoordinator
import com.dignicate.kmpstarter.core.MainTab
import com.dignicate.kmpstarter.ui.components.MainNavigationContainer
import com.dignicate.kmpstarter.ui.screens.LaunchScreen
import com.dignicate.kmpstarter.ui.screens.SettingsScreen
import com.dignicate.kmpstarter.core.CommonBackHandler
import com.dignicate.kmpstarter.core.getAppVersion

enum class Screen {
    Launch, Home, Settings
}

@Composable
fun App() {
    val appConfig = remember { 
        AppConfig(
            version = getAppVersion(),
            environment = "dev" 
        ) 
    }
    var currentScreen by remember { mutableStateOf(Screen.Launch) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    val coordinator = remember {
        object : Coordinator {
            // ... (keep previous overrides)
            override fun goToLaunch() { currentScreen = Screen.Launch }
            override fun goToHome() {
                currentScreen = Screen.Home
                selectedTab = MainTab.HOME
            }
            override fun goToCatalog() {
                currentScreen = Screen.Home
                selectedTab = MainTab.CATALOG
            }
            override fun goToSaved() {
                currentScreen = Screen.Home
                selectedTab = MainTab.SAVED
            }
            override fun goToMenu() {
                currentScreen = Screen.Home
                selectedTab = MainTab.MENU
            }
            override fun goToSettings() {
                currentScreen = Screen.Settings
            }
            override fun pop() {
                when (currentScreen) {
                    Screen.Settings -> currentScreen = Screen.Home
                    // Home画面ではこれ以上戻らない（OSに任せる）
                    else -> {}
                }
            }
        }
    }

    // Home画面（ルート）以外の時だけ、カスタムの戻る処理（coordinator.pop）を有効にする
    CommonBackHandler(enabled = currentScreen == Screen.Settings) {
        coordinator.pop()
    }

    CompositionLocalProvider(
        LocalAppConfig provides appConfig,
        LocalCoordinator provides coordinator
    ) {
        MaterialTheme {
            when (currentScreen) {
                Screen.Launch -> LaunchScreen()
                Screen.Home -> MainNavigationContainer(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                Screen.Settings -> SettingsScreen()
            }
        }
    }
}
