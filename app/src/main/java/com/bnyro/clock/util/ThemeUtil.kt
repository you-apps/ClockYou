package com.bnyro.clock.util

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.Scheme

object ThemeUtil {
    @SuppressLint("RestrictedApi")
    fun getSchemeFromSeed(color: Int, dark: Boolean): ColorScheme {
        return if (dark) {
            Scheme.dark(color).toColorScheme()
        } else {
            Scheme.light(color).toColorScheme()
        }
    }
}

val catpucchinLatte = arrayOf(
    android.graphics.Color.rgb(220, 138, 120),
    android.graphics.Color.rgb(221, 120, 120),
    android.graphics.Color.rgb(234, 118, 203),
    android.graphics.Color.rgb(136, 57, 239),
    android.graphics.Color.rgb(210, 15, 57),
    android.graphics.Color.rgb(230, 69, 83),
    android.graphics.Color.rgb(254, 100, 11),
    android.graphics.Color.rgb(223, 142, 29),
    android.graphics.Color.rgb(64, 160, 43),
    android.graphics.Color.rgb(23, 146, 153),
    android.graphics.Color.rgb(4, 165, 229),
    android.graphics.Color.rgb(32, 159, 181),
    android.graphics.Color.rgb(30, 102, 245),
    android.graphics.Color.rgb(114, 135, 253)
)

@SuppressLint("RestrictedApi")
fun Scheme.toColorScheme(): ColorScheme {
    val surf = Color(surface)
    val surfVar = Color(surfaceVariant)
    val tint = Color(primary)
    fun elevate(base: Color, amount: Float) = androidx.compose.ui.graphics.lerp(base, tint, amount)

    return ColorScheme(
        primary = Color(primary),
        onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer),
        onPrimaryContainer = Color(onPrimaryContainer),
        inversePrimary = Color(inversePrimary),
        secondary = Color(secondary),
        onSecondary = Color(onSecondary),
        secondaryContainer = Color(secondaryContainer),
        onSecondaryContainer = Color(onSecondaryContainer),
        tertiary = Color(tertiary),
        onTertiary = Color(onTertiary),
        tertiaryContainer = Color(tertiaryContainer),
        onTertiaryContainer = Color(onTertiaryContainer),
        background = Color(background),
        onBackground = Color(onBackground),
        surface = surf,
        onSurface = Color(onSurface),
        surfaceVariant = surfVar,
        onSurfaceVariant = Color(onSurfaceVariant),
        surfaceTint = tint,
        inverseSurface = Color(inverseSurface),
        inverseOnSurface = Color(inverseOnSurface),
        error = Color(error),
        onError = Color(onError),
        errorContainer = Color(errorContainer),
        onErrorContainer = Color(onErrorContainer),
        outline = Color(outline),
        outlineVariant = Color(outlineVariant),
        scrim = Color(scrim),
        surfaceContainerLowest = surf,
        surfaceContainerLow = elevate(surf, 0.05f),
        surfaceContainer = elevate(surf, 0.08f),
        surfaceContainerHigh = elevate(surf, 0.11f),
        surfaceContainerHighest = elevate(surf, 0.14f),
        surfaceBright = elevate(surf, 0.12f),
        surfaceDim = surf
    )
}