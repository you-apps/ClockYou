@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)

package com.bnyro.clock.presentation.screens.stopwatch

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.ui.theme.ItemFade
import com.bnyro.clock.ui.theme.ItemSlide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.bnyro.clock.util.extensions.KeepScreenOn

private fun <T> bouncySpring() = spring<T>(dampingRatio = 0.35f, stiffness = 450f)

private val ControlHeight = 80.dp
private val PrimaryHeight = 96.dp

@Composable
fun StopwatchScreen(onClickSettings: () -> Unit, stopwatchModel: StopwatchModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val horizontalPadding = if (screenWidth < 360.dp) 8.dp else 16.dp

    val scope = rememberCoroutineScope()
    val timeStampsState = rememberLazyListState()

    val laps = stopwatchModel.rememberedTimeStamps
    val maxVisibleLapsBeforeScroll = if (screenHeight < 650.dp) 2 else 3
    val approxRowHeightDp = 48.dp
    val headerHeightDp = 30.dp
    val calculatedMaxHeight = (approxRowHeightDp * maxVisibleLapsBeforeScroll) + headerHeightDp
    val showProgress = if (laps.isEmpty()) {
        screenHeight >= 500.dp
    } else {
        screenHeight >= 720.dp
    }

    TopBarScaffold(title = stringResource(R.string.stopwatch), onClickSettings) { pv ->
        if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeDisplay(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding, vertical = 12.dp)
                        .weight(2f),
                    innerModifier = Modifier
                        .heightIn(0.dp, 340.dp)
                        .fillMaxWidth(),
                    stopwatchModel = stopwatchModel,
                    showProgress = showProgress,
                    onClick = { stopwatchModel.pauseResumeStopwatch(context) }
                )
                AnimatedVisibility(laps.isNotEmpty()) {
                    val lapListModifier = if (laps.size > maxVisibleLapsBeforeScroll) {
                        Modifier
                            .height(calculatedMaxHeight)
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 16.dp)
                    } else {
                        Modifier
                            .heightIn(0.dp, calculatedMaxHeight)
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 16.dp)
                    }

                    LapList(
                        modifier = lapListModifier,
                        stopwatchModel = stopwatchModel,
                        timeStampsState = timeStampsState
                    )
                }
                StopwatchController(stopwatchModel, scope, timeStampsState, context)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimeDisplay(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        innerModifier = Modifier.fillMaxSize(),
                        stopwatchModel = stopwatchModel,
                        showProgress = false,
                        onClick = { stopwatchModel.pauseResumeStopwatch(context) }
                    )
                    StopwatchController(
                        stopwatchModel = stopwatchModel,
                        scope = scope,
                        timeStampsState = timeStampsState,
                        context = context
                    )
                }
                if (laps.isNotEmpty()) {
                    LapList(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontalPadding),
                        stopwatchModel = stopwatchModel,
                        timeStampsState = timeStampsState
                    )
                }
            }
        }
    }
    if (stopwatchModel.state == WatchState.RUNNING) {
        KeepScreenOn()
    }
}

@Composable
private fun StopwatchController(
    stopwatchModel: StopwatchModel,
    scope: CoroutineScope,
    timeStampsState: LazyListState,
    context: Context
) {
    val running = stopwatchModel.state == WatchState.RUNNING
    var lapTicks by remember { mutableIntStateOf(0) }
    val lapIconRotation by animateFloatAsState(
        targetValue = lapTicks * 90f,
        animationSpec = bouncySpring(),
        label = "lapIconRotation"
    )

    Row(
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SideSlot(visible = running, edge = Alignment.End) {
            ExpressiveButton(
                modifier = Modifier.height(ControlHeight),
                restCorner = 28.dp,
                pressedCorner = 12.dp,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                hapticType = HapticFeedbackType.LongPress,
                onClick = {
                    val tableOverflows = timeStampsState.canScrollForward ||
                            timeStampsState.canScrollBackward
                    stopwatchModel.onLapClicked()
                    lapTicks++
                    if (tableOverflows) {
                        scope.launch {
                            timeStampsState.scrollToItem(
                                stopwatchModel.rememberedTimeStamps.size - 1
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(lapIconRotation)
                )
                Text(
                    text = stringResource(R.string.lap),
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
            }
        }

        ExpressiveButton(
            modifier = Modifier
                .height(PrimaryHeight)
                .width(150.dp),
            restCorner = if (running) 32.dp else PrimaryHeight / 2,
            pressedCorner = 16.dp,
            hapticType = HapticFeedbackType.Confirm,
            onClick = { stopwatchModel.pauseResumeStopwatch(context) }
        ) {
            AnimatedContent(
                targetState = running,
                transitionSpec = {
                    (scaleIn(bouncySpring(), initialScale = 0.3f) + fadeIn()) togetherWith
                            (scaleOut(targetScale = 0.3f) + fadeOut())
                },
                label = "playPause"
            ) { isRunning ->
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        SideSlot(visible = stopwatchModel.currentPosition != 0L, edge = Alignment.Start) {
            if (stopwatchModel.state != WatchState.PAUSED) {
                ExpressiveButton(
                    modifier = Modifier.size(ControlHeight),
                    restCorner = 28.dp,
                    pressedCorner = 12.dp,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    hapticType = HapticFeedbackType.LongPress,
                    onClick = { stopwatchModel.stopStopwatch(context) }
                ) {
                    Icon(Icons.Default.Stop, null, Modifier.size(32.dp))
                }
            } else {
                ExpressiveButton(
                    modifier = Modifier.size(ControlHeight),
                    restCorner = 28.dp,
                    pressedCorner = 12.dp,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    hapticType = HapticFeedbackType.LongPress,
                    onClick = {
                        stopwatchModel.stopStopwatch(context)
                        stopwatchModel.rememberedTimeStamps.clear()
                    }
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpressiveButton(
    onClick: () -> Unit,
    restCorner: Dp,
    pressedCorner: Dp,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    hapticType: HapticFeedbackType = HapticFeedbackType.Confirm,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val corner by animateDpAsState(
        targetValue = if (pressed) pressedCorner else restCorner,
        animationSpec = bouncySpring(),
        label = "corner"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = bouncySpring(),
        label = "scale"
    )

    Button(
        onClick = {
            haptics.performHapticFeedback(hapticType)
            onClick()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun SideSlot(
    visible: Boolean,
    edge: Alignment.Horizontal,
    content: @Composable () -> Unit
) {
    val expandFrom = if (edge == Alignment.End) Alignment.End else Alignment.Start
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(bouncySpring(), expandFrom = expandFrom) + fadeIn(),
        exit = shrinkHorizontally(bouncySpring(), shrinkTowards = expandFrom) + fadeOut()
    ) {
        Box(
            modifier = Modifier.padding(
                start = if (edge == Alignment.Start) 6.dp else 0.dp,
                end = if (edge == Alignment.End) 6.dp else 0.dp
            )
        ) {
            content()
        }
    }
}

@Composable
private fun LapList(
    modifier: Modifier = Modifier,
    stopwatchModel: StopwatchModel,
    timeStampsState: LazyListState
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val laps = stopwatchModel.rememberedTimeStamps

    val best: Any? = null
    val worst: Any? = null

    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (laps.isNotEmpty()) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val formattedLaps = laps
                            .mapIndexed { index, time ->
                                val lapNumber = String.format("%02d", index + 1)
                                val lapTime = time.second.toFullString()
                                val overallTime = time.first.toFullString()
                                "Lap $lapNumber: $lapTime (Total: $overallTime)"
                            }
                            .joinToString(separator = "\n")
                        clipboardManager.setText(AnnotatedString(formattedLaps))
                        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                    }
                }
            ),
        state = timeStampsState,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
            ) {
                HeaderLabel(stringResource(R.string.lap), Modifier.weight(0.5f), TextAlign.Start)
                HeaderLabel(stringResource(R.string.lap_time), Modifier.weight(1.1f), TextAlign.End)
                HeaderLabel(stringResource(R.string.overall_time), Modifier.weight(1.1f), TextAlign.End)
            }
        }
        itemsIndexed(
            items = laps,
            key = { index, _ -> index }
        ) { index, time ->
            val outer = 20.dp
            val inner = 4.dp
            val shape = RoundedCornerShape(
                topStart = if (index == 0) outer else inner,
                topEnd = if (index == 0) outer else inner,
                bottomStart = if (index == laps.lastIndex) outer else inner,
                bottomEnd = if (index == laps.lastIndex) outer else inner
            )

            val isBest = best === time
            val isWorst = worst === time
            val container by animateColorAsState(
                when {
                    isBest -> MaterialTheme.colorScheme.tertiaryContainer
                    isWorst -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                label = "lapContainer"
            )
            val content = when {
                isBest -> MaterialTheme.colorScheme.onTertiaryContainer
                isWorst -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
            val numeric = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")

            Row(
                modifier = Modifier
                    .animateItem(ItemFade, ItemSlide, ItemFade)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(container)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d", index + 1),
                    modifier = Modifier.weight(0.5f),
                    style = numeric,
                    color = content.copy(alpha = 0.7f)
                )
                Text(
                    text = time.second.toFullString(),
                    modifier = Modifier.weight(1.1f),
                    style = numeric.copy(fontWeight = FontWeight.SemiBold),
                    color = content,
                    textAlign = TextAlign.End
                )
                Text(
                    text = time.first.toFullString(),
                    modifier = Modifier.weight(1.1f),
                    style = numeric,
                    color = content.copy(alpha = 0.7f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier, align: TextAlign) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align
    )
}

@Composable
private fun TimeDisplay(
    modifier: Modifier = Modifier,
    innerModifier: Modifier,
    stopwatchModel: StopwatchModel,
    showProgress: Boolean = true,
    onClick: () -> Unit
) {
    val running = stopwatchModel.state == WatchState.RUNNING
    val haptics = LocalHapticFeedback.current
    val timeColor by animateColorAsState(
        targetValue = if (running) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "timeColor"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = innerModifier,
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                val hours = stopwatchModel.currentPosition / 3_600_000
                val minutes = stopwatchModel.currentPosition / 60_000 % 60
                val seconds = stopwatchModel.currentPosition / 1000 % 60
                val hundredths = stopwatchModel.currentPosition / 10 % 100
                val separator = SpanStyle(baselineShift = BaselineShift(0.1f))
                val displayStyle = MaterialTheme.typography.displayLargeEmphasized
                BasicText(
                    text = buildAnnotatedString {
                        if (hours > 0) {
                            append(hours.toString())
                            withStyle(separator) { append(":") }
                        }
                        append(if (hours > 0) minutes.toString().padStart(2, '0') else minutes.toString())
                        withStyle(separator) { append(":") }
                        append(seconds.toString().padStart(2, '0'))
                        withStyle(
                            SpanStyle(
                                fontSize = 0.5.em,
                                fontWeight = FontWeight.Normal,
                                color = timeColor.copy(alpha = 0.6f)
                            )
                        ) {
                            append(" ")
                            append(hundredths.toString().padStart(2, '0'))
                        }
                    },
                    modifier = Modifier.padding(horizontal = if (showProgress) 24.dp else 16.dp),
                    style = displayStyle.copy(
                        color = timeColor,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp,
                        maxFontSize = 80.sp,
                        stepSize = 1.sp
                    )
                )
            }
        }
        if (showProgress) {
            CircularProgressIndicator(
                progress = { (stopwatchModel.currentPosition % 60000) / 60000f },  //apparently slightly better and removes circularprogressindicator from stressing and giving a warn idk dud
                modifier = Modifier
                    .heightIn(0.dp, 340.dp)
                    .aspectRatio(1f, true)
                    .padding(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
        }
    }
}