package com.dignicate.kmpstarter.core

data class AppConfig(
    val version: String,
    val env: AppEnvironment,
    val packageName: String,
    val isDebug: Boolean,
) {
    val showsDebugMenu: Boolean get() = !env.isProduction
}
