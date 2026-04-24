package com.dignicate.kmpstarter.core

import androidx.compose.runtime.staticCompositionLocalOf

data class AppConfig(
    val version: String,
    val environment: String
)

val LocalAppConfig = staticCompositionLocalOf<AppConfig> {
    error("No AppConfig provided")
}
