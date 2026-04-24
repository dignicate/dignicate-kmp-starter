package com.dignicate.kmpstarter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dignicate.kmpstarter.core.LocalAppConfig
import com.dignicate.kmpstarter.core.LocalCoordinator

@Composable
fun AppDrawer(
    onClose: () -> Unit
) {
    val coordinator = LocalCoordinator.current
    val appConfig = LocalAppConfig.current

    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // A. Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // B. Navigation Items Section
            Spacer(modifier = Modifier.height(12.dp))
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                selected = false,
                onClick = {
                    coordinator.goToSettings()
                    onClose()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // C. Version Info Section (Footer)
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "v${appConfig.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
