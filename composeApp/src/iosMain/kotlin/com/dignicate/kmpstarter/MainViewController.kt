package com.dignicate.kmpstarter

import androidx.compose.ui.window.ComposeUIViewController
import com.dignicate.kmpstarter.providers.initKoin
import platform.UIKit.UIViewController

class MainViewControllerFactory {
    fun make(): UIViewController {
        initKoin()
        return ComposeUIViewController {
            App()
        }
    }
}
