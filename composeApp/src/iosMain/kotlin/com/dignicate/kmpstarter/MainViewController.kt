package com.dignicate.kmpstarter

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

class MainViewControllerFactory {
    fun make(): UIViewController =
        ComposeUIViewController {
            App()
        }
}
