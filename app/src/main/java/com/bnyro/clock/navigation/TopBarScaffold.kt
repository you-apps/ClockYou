package com.bnyro.clock.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.bnyro.clock.presentation.components.ClickableIcon
import androidx.compose.material3.FabPosition
import androidx.compose.ui.Modifier
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScaffold(
    title: String,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
    fab: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = { Text(title) }, actions = {
                actions()
                ClickableIcon(imageVector = Icons.Default.Settings) {
                    onClickSettings()
                }
            })
        },
        floatingActionButton = fab,
        floatingActionButtonPosition = fabPosition
    ) { it ->
        content(it)
    }
}