package com.dignicate.kmpstarter.core

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a system back button like Android.
    // Navigation is usually handled by UI buttons or swipe gestures.
}
