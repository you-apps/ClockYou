package com.bnyro.clock.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * [ColorScheme.primary] at the weight a control takes when it sits behind the one in focus.
 */
val ColorScheme.primaryFade: Color get() = primary.copy(alpha = 0.3f)

/**
 * [ColorScheme.primary] at the weight a control takes when it carries no focus of its own.
 */
val ColorScheme.primarySoft: Color get() = primary.copy(alpha = 0.6f)

val ColorScheme.ringingOuterRing: Color get() = onSurface.copy(alpha = 0.035f)
val ColorScheme.ringingPulse: Color get() = primary.copy(alpha = 0.06f)
val ColorScheme.ringingHalo: Color get() = onSurface.copy(alpha = 0.08f)
val ColorScheme.ringingTarget: Color get() = primary.copy(alpha = 0.18f)
val ColorScheme.ringingTargetOutline: Color get() = primary.copy(alpha = 0.35f)
val ColorScheme.ringingHandleDragged: Color get() = primary.copy(alpha = 0.7f)
