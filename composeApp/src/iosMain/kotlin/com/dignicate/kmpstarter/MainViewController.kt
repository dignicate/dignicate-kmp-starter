package com.dignicate.kmpstarter

import androidx.compose.ui.window.ComposeUIViewController
import com.dignicate.kmpstarter.core.AppConfig
import com.dignicate.kmpstarter.core.AppEnvironment
import com.dignicate.kmpstarter.providers.initKoin
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

class MainViewControllerFactory {
    fun make(): UIViewController {
        initKoin(buildAppConfig())
        return ComposeUIViewController {
            App()
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    private fun buildAppConfig(): AppConfig {
        val bundle = NSBundle.mainBundle
        val info = bundle.infoDictionary
        val version = info?.get("CFBundleShortVersionString") as? String ?: "0.0.0"
        val envName = info?.get("APP_ENV") as? String ?: "UNKNOWN"
        return AppConfig(
            version = version,
            env = AppEnvironment.fromName(envName),
            packageName = bundle.bundleIdentifier ?: "unknown",
            isDebug = Platform.isDebugBinary,
        )
    }
}
