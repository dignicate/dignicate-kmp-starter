package com.dignicate.kmpstarter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dignicate.kmpstarter.core.LocalCoordinator

@Composable
fun AppDrawer(
    onClose: () -> Unit
) {
    val coordinator = LocalCoordinator.current

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = {
                coordinator.goToSettings()
                onClose()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        // Add more items if needed
    }
}
