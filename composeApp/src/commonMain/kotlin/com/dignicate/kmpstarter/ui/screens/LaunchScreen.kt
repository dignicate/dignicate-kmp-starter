package com.dignicate.kmpstarter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dignicate.kmpstarter.core.LocalCoordinator
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen() {
    val coordinator = LocalCoordinator.current

    LaunchedEffect(Unit) {
        delay(1000) // Simulate some initialization
        coordinator.goToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
