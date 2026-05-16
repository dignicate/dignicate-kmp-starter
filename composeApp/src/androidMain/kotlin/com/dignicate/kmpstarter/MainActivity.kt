package com.dignicate.kmpstarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dignicate.kmpstarter.core.AppConfig
import com.dignicate.kmpstarter.core.AppEnvironment
import com.dignicate.kmpstarter.providers.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKoin(buildAppConfig())
        setContent {
            App()
        }
    }

    private fun buildAppConfig(): AppConfig {
        val pkgInfo = packageManager.getPackageInfo(packageName, 0)
        return AppConfig(
            version = pkgInfo.versionName ?: "0.0.0",
            env = AppEnvironment.fromName(BuildConfig.APP_ENV),
            packageName = packageName,
            isDebug = BuildConfig.DEBUG,
        )
    }
}
