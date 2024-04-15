package com.bnyro.clock.util.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

// https://stackoverflow.com/a/71293123/9652621
@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true

        onDispose {
            currentView.keepScreenOn = false
        }
    }
}