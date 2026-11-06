package com.bnyro.clock.presentation.screens.ringing

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.text.format.DateUtils
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.ui.theme.ringingHandleDragged
import com.bnyro.clock.ui.theme.ringingHalo
import com.bnyro.clock.ui.theme.ringingOuterRing
import com.bnyro.clock.ui.theme.ringingPulse
import com.bnyro.clock.ui.theme.ringingTarget
import com.bnyro.clock.ui.theme.ringingTargetOutline
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RingingAlert(
    icon: Painter,
    onDismiss: () -> Unit,
    title: @Composable () -> Unit,
    controls: @Composable () -> Unit
) {
    val settingsModel: SettingsModel = viewModel()
    ClockYouTheme(
        darkTheme = true,
        customColorScheme = ThemeUtil.getSchemeFromSeed(settingsModel.customColor, true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                if (LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT) {
                    val iconSize = (maxHeight * 0.19f).coerceIn(72.dp, 144.dp)
                    Column(
                        modifier = Modifier.fillMaxSize().padding(
                            start = 24.dp,
                            top = (maxHeight * 0.04f).coerceIn(12.dp, 32.dp),
                            end = 24.dp,
                            bottom = 16.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RingingIcon(icon, iconSize)
                        Spacer(Modifier.height(12.dp))
                        title()
                        Box(Modifier.fillMaxWidth().weight(1f)) {
                            RingingDismiss(onDismiss)
                        }
                        controls()
                    }
                } else {
                    val iconSize = (maxHeight * 0.32f).coerceIn(64.dp, 144.dp)
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RingingIcon(icon, iconSize)
                            Spacer(Modifier.height(12.dp))
                            title()
                        }
                        Column(
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.fillMaxWidth().weight(1f)) {
                                RingingDismiss(onDismiss)
                            }
                            controls()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RingingTitle(
    label: String?,
    ringingTime: String,
    showSeconds: Boolean = false,
    ringingSeconds: Long? = null
) {
    val context = LocalContext.current
    val now by produceState(
        initialValue = TimeHelper.formatTime(context, TimeHelper.getTimeByZone(), showSeconds),
        showSeconds
    ) {
        while (isActive) {
            value = TimeHelper.formatTime(context, TimeHelper.getTimeByZone(), showSeconds)
            delay(1000)
        }
    }
    val hasLabel = !label.isNullOrBlank()
    var showingLabel by rememberSaveable(label, ringingTime) { mutableStateOf(false) }
    LaunchedEffect(label, ringingTime) {
        if (hasLabel) {
            while (isActive) {
                delay(4000)
                showingLabel = !showingLabel
            }
        }
    }
    val pillTextStyle = MaterialTheme.typography.titleMedium.copy(lineHeight = TextUnit.Unspecified)
    val pillIconSize = with(LocalDensity.current) { (pillTextStyle.fontSize * 1.25f).toDp() }
    val pillAutoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = pillTextStyle.fontSize, stepSize = 1.sp)
    val pillHeight = maxOf(40.dp, with(LocalDensity.current) {
        MaterialTheme.typography.titleMedium.lineHeight.toDp()
    } + 16.dp)
    val titleGap = if (showSeconds) 16.dp else 12.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val clockHeight = minOf(
            if (showSeconds) 92.dp else 120.dp,
            maxHeight - maxOf(48.dp, pillHeight) - titleGap
        ).coerceAtLeast(24.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                text = now,
                modifier = Modifier.fillMaxWidth().height(clockHeight),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Light,
                    lineHeight = TextUnit.Unspecified,
                    fontFeatureSettings = "tnum",
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 24.sp,
                    maxFontSize = if (showSeconds) 76.sp else 108.sp,
                    stepSize = 1.sp
                )
            )
            Spacer(Modifier.height(titleGap))
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val timePillWidth = maxWidth
                val elapsedPillWidth = (maxWidth - 8.dp) / 2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { if (hasLabel) showingLabel = !showingLabel },
                        enabled = hasLabel,
                        modifier = Modifier
                            .then(if (ringingSeconds != null) Modifier.weight(1f, fill = false) else Modifier)
                            .widthIn(max = timePillWidth)
                            .height(pillHeight),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Crossfade(targetState = showingLabel && hasLabel, label = "Ringing label") { showLabel ->
                            Row(
                                modifier = Modifier
                                    .widthIn(min = if (hasLabel) minOf(176.dp, timePillWidth) else 0.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (showLabel) Icons.AutoMirrored.Outlined.Label else Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(pillIconSize)
                                )
                                Spacer(Modifier.width(8.dp))
                                BasicText(
                                    text = if (showLabel) label.orEmpty() else ringingTime,
                                    style = pillTextStyle.copy(color = LocalContentColor.current),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    autoSize = if (showLabel) null else pillAutoSize
                                )
                            }
                        }
                    }
                    ringingSeconds?.let { seconds ->
                        Surface(
                            modifier = Modifier.widthIn(max = elapsedPillWidth).height(pillHeight),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassEmpty,
                                    contentDescription = null,
                                    modifier = Modifier.size(pillIconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                BasicText(
                                    text = "+" + DateUtils.formatElapsedTime(seconds),
                                    style = pillTextStyle.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    autoSize = pillAutoSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RingingIcon(icon: Painter, size: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "Ringing icon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(400, easing = EaseInOutBack), RepeatMode.Reverse),
        label = "Ringing shake"
    )
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(200, easing = Ease), RepeatMode.Reverse),
        label = "Ringing bounce"
    )
    Image(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.size(size).offset(y = verticalOffset.dp).rotate(rotation),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun RingingDismiss(onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val dismissDescription = stringResource(R.string.dismiss)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    var dragging by remember { mutableStateOf(false) }
    var dismissReady by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val handleOffset by animateOffsetAsState(
        targetValue = dragOffset,
        animationSpec = spring(
            dampingRatio = if (dragging) Spring.DampingRatioNoBouncy else 0.8f,
            stiffness = if (dragging) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "Dismiss motion"
    )
    val handleColor by animateColorAsState(
        targetValue = when {
            dismissReady -> colors.tertiaryContainer
            dragging -> colors.ringingHandleDragged
            else -> colors.primary
        },
        animationSpec = tween(120),
        label = "Dismiss handle"
    )
    val contentColor by animateColorAsState(
        targetValue = if (dismissReady) colors.onTertiaryContainer else colors.onPrimary,
        animationSpec = tween(120),
        label = "Dismiss icon"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "Dismiss attention")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "Dismiss breathing"
    )
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val handleRatio = 4f / 7f
        val diameter = minOf(maxWidth / (1f + handleRatio), maxHeight / (1f + handleRatio), 280.dp)
        val handleDiameter = diameter * handleRatio
        val handleRadius = with(LocalDensity.current) { (handleDiameter / 2).toPx() }
        val travelDistance = with(LocalDensity.current) { (diameter / 2).toPx() }
        val dismissDistance = travelDistance * 0.9f
        val handleDistance = handleOffset.getDistance()
        val boundedHandleOffset = if (handleDistance > travelDistance) {
            handleOffset * (travelDistance / handleDistance)
        } else {
            handleOffset
        }
        val currentHandleOffset by rememberUpdatedState(boundedHandleOffset)
        Box(
            modifier = Modifier
                .size(diameter + handleDiameter)
                .pointerInput(travelDistance, haptics) {
                    var dragOrigin = Offset.Zero
                    try {
                        detectDragGestures(
                            orientationLock = null,
                            onDragStart = { down, _, _ ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                dragging = (down.position - center - currentHandleOffset).getDistance() <= handleRadius
                                if (dragging) {
                                    dragOrigin = down.position - currentHandleOffset
                                    dragOffset = currentHandleOffset
                                    dismissReady = false
                                }
                            },
                            onDragEnd = {
                                if (dragging) {
                                    val dismiss = dismissReady
                                    dragOffset = Offset.Zero
                                    dragging = false
                                    dismissReady = false
                                    if (dismiss) currentOnDismiss()
                                }
                            },
                            onDragCancel = {
                                dragOffset = Offset.Zero
                                dragging = false
                                dismissReady = false
                            }
                        ) { change, _ ->
                            if (dragging) {
                                change.consume()
                                val offset = change.position - dragOrigin
                                val distance = offset.getDistance()
                                dragOffset = if (distance > travelDistance) {
                                    offset * (travelDistance / distance)
                                } else {
                                    offset
                                }
                                val nextDismissReady = distance >= dismissDistance
                                if (nextDismissReady && !dismissReady) {
                                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                }
                                dismissReady = nextDismissReady
                            }
                        }
                    } finally {
                        dragOffset = Offset.Zero
                        dragging = false
                        dismissReady = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(diameter)) {
                val radius = size.minDimension / 2
                drawCircle(colors.ringingOuterRing, radius = radius - 1.5.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                drawCircle(colors.ringingPulse, radius = radius * (0.84f + pulse * 0.12f), style = Stroke(1.5.dp.toPx()))
                drawCircle(colors.ringingHalo, radius = radius * 0.86f)
                drawCircle(colors.ringingTarget, radius = radius * 0.73f)
                drawCircle(colors.ringingTargetOutline, radius = radius * 0.73f, style = Stroke(1.5.dp.toPx()))
                drawArc(
                    color = colors.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * (boundedHandleOffset.getDistance() / dismissDistance).coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
                    size = size.copy(width = size.width - 3.dp.toPx(), height = size.height - 3.dp.toPx()),
                    style = Stroke(1.5.dp.toPx())
                )
            }
            Surface(
                modifier = Modifier
                    .size(handleDiameter)
                    .graphicsLayer {
                        translationX = boundedHandleOffset.x
                        translationY = boundedHandleOffset.y
                    }
                    .semantics {
                        contentDescription = dismissDescription
                        role = Role.Button
                        onClick(label = dismissDescription) {
                            currentOnDismiss()
                            true
                        }
                    },
                shape = CircleShape,
                color = handleColor,
                contentColor = contentColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(diameter * 0.2f)
                    )
                }
            }
        }
    }
}
