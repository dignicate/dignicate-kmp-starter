package com.dignicate.kmpstarter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.dignicate.kmpstarter.ui.MainTab
import com.dignicate.kmpstarter.ui.screens.CatalogTabScreen
import com.dignicate.kmpstarter.ui.screens.HomeTabScreen
import com.dignicate.kmpstarter.ui.screens.MenuTabScreen
import com.dignicate.kmpstarter.ui.screens.SavedTabScreen
import kotlinx.coroutines.launch

@Composable
fun MainNavigationContainer(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    version: String,
    onOpenSettings: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val saveableStateHolder = rememberSaveableStateHolder()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                version = version,
                onOpenSettings = onOpenSettings,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CustomAppBar(
                    title = selectedTab.label,
                    showMenuButton = true,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onTabSelected(tab) },
                            label = { Text(tab.label) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        MainTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                        MainTab.CATALOG -> if (selected) {
                                            Icons.AutoMirrored.Filled.List
                                        } else {
                                            Icons.AutoMirrored.Outlined.List
                                        }
                                        MainTab.SAVED -> if (selected) {
                                            Icons.Filled.Favorite
                                        } else {
                                            Icons.Outlined.FavoriteBorder
                                        }
                                        MainTab.MENU -> if (selected) Icons.Filled.Menu else Icons.Outlined.Menu
                                    },
                                    contentDescription = tab.label
                                )
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                saveableStateHolder.SaveableStateProvider(selectedTab) {
                    when (selectedTab) {
                        MainTab.HOME -> HomeTabScreen()
                        MainTab.CATALOG -> CatalogTabScreen()
                        MainTab.SAVED -> SavedTabScreen()
                        MainTab.MENU -> MenuTabScreen()
                    }
                }
            }
        }
    }
}
