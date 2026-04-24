package com.dignicate.kmpstarter.core

import androidx.compose.runtime.staticCompositionLocalOf

enum class MainTab(val label: String) {
    HOME("Home"),
    CATALOG("Catalog"),
    SAVED("Saved"),
    MENU("Menu")
}

interface Coordinator {
    fun goToLaunch()
    fun goToHome()
    fun goToCatalog()
    fun goToSaved()
    fun goToMenu()
    fun goToSettings()
    fun pop()
}

val LocalCoordinator = staticCompositionLocalOf<Coordinator> {
    error("No Coordinator provided")
}
