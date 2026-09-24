package com.bnyro.clock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun ClockYouTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customColorScheme: ColorScheme,
    dynamicColor: Boolean = true,
    amoledDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        amoledDark -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                dynamicDarkColorScheme(context).copy(
                    background = Color.Black,
                    surface = Color.Black
                )
            } else {
                customColorScheme.copy(background = Color.Black, surface = Color.Black)
            }
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> customColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val insetsController = WindowCompat.getInsetsController(
                    activity.window,
                    view
                )
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
