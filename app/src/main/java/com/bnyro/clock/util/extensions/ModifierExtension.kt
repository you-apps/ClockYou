// Taken from https://github.com/sadellie/unitto/blob/d6f8d2e9127ebaf30c77bdf8d4bbe69348b93481/core/ui/src/main/java/com/sadellie/unitto/core/ui/common/ModifierExtensions.kt#L37
package com.bnyro.clock.util.extensions

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.squashable(
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    cornerRadiusRange: IntRange,
    role: Role = Role.Button
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by animateIntAsState(
        targetValue = if (isPressed) cornerRadiusRange.first else cornerRadiusRange.last,
        animationSpec = tween(easing = FastOutSlowInEasing)
    )

    Modifier
        .clip(RoundedCornerShape(cornerRadius))
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interactionSource,
            indication = rememberRipple(),
            role = role,
            enabled = enabled
        )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.squashable(
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    cornerRadiusRange: ClosedRange<Dp>,
    role: Role = Role.Button
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius: Dp by animateDpAsState(
        targetValue = if (isPressed) cornerRadiusRange.start else cornerRadiusRange.endInclusive,
        animationSpec = tween(easing = FastOutSlowInEasing)
    )

    Modifier
        .clip(RoundedCornerShape(cornerRadius))
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interactionSource,
            indication = rememberRipple(),
            role = role,
            enabled = enabled
        )
}
