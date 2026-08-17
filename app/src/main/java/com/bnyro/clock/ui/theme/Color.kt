package com.bnyro.clock.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * A shaded companion to [ColorScheme.primaryContainer], for the subordinate half of a control
 * that shares one container with a primary action.
 */
val ColorScheme.primaryContainerShade: Color
    get() = lerp(primaryContainer, Color.Black, 0.12f)
