package com.bnyro.clock.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.bnyro.clock.presentation.components.ClickableIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScaffold(
    title: String,
    onClickSettings: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(title)
            },
            actions = {
                actions()
                ClickableIcon(imageVector = Icons.Default.Settings) {
                    onClickSettings()
                }
            }
        )
    }, floatingActionButton = fab) {
        content(it)
    }
}
