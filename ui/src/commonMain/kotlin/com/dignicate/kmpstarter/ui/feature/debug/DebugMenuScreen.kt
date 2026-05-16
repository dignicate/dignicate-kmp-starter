package com.dignicate.kmpstarter.ui.feature.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dignicate.kmpstarter.ui.components.CustomAppBar
import com.dignicate.kmpstarter.viewmodel.feature.debug.DebugMenuViewModel

@Composable
fun DebugMenuTabScreen(
    viewModel: DebugMenuViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    DebugMenuScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

@Composable
fun DebugMenuScreen(
    uiState: DebugMenuViewModel.UiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Debug Menu",
                showBackButton = true,
                onBackClick = onBack,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.showsAppInfo) {
                DebugAppInfoFooter(
                    packageName = uiState.packageName,
                    version = uiState.version,
                    environment = uiState.environment.rawName,
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Future debug actions go here.
                }
            }
        }
    }
}

@Composable
private fun DebugAppInfoFooter(
    packageName: String,
    version: String,
    environment: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        InfoRow(label = "Package Name", value = packageName)
        InfoRow(label = "Version", value = version)
        InfoRow(label = "Environment", value = environment)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Box(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = buildLabel(label, value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun buildLabel(label: String, value: String) =
    androidx.compose.ui.text.buildAnnotatedString {
        pushStyle(
            androidx.compose.ui.text.SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        )
        append("$label: ")
        pop()
        append(value)
    }
