package com.bnyro.clock.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

object ScreenControlHelper {

    private var currentState = false

    // https://stackoverflow.com/a/71293123/9652621
    @Composable
    fun KeepScreenOn() {
        if (!currentState) {
            val currentView = LocalView.current
            DisposableEffect(Unit) {
                currentView.keepScreenOn = true
                currentState = true
                onDispose {
                    currentView.keepScreenOn = false
                    currentState = false
                }
            }
        }
    }
}