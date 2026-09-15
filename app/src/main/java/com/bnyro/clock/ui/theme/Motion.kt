package com.bnyro.clock.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The weight a card takes on as it arrives in a list, and gives up as it leaves.
 */
const val ItemFadeDurationMillis = 120
val ItemFade: FiniteAnimationSpec<Float> =
    tween(durationMillis = ItemFadeDurationMillis, easing = FastOutSlowInEasing)

/**
 * The travel of a card the list moves aside to make room for another, or to close the gap one left.
 */
val ItemSlide: FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = 220, easing = FastOutSlowInEasing)

/**
 * The growth of a list that has gained a row, or the shrink of one that has lost it.
 */
val ListResize: FiniteAnimationSpec<IntSize> =
    tween(durationMillis = 220, easing = FastOutSlowInEasing)
