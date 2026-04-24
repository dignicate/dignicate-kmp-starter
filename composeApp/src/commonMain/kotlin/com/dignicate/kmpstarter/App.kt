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
            override fun goToLaunch() {
                currentScreen = Screen.Launch
            }
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
                    Screen.Home -> currentScreen = Screen.Launch
                    else -> {}
                }
            }
        }
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
