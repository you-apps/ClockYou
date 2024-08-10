package com.bnyro.clock.presentation.screens.stopwatch

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.util.extensions.KeepScreenOn
import com.bnyro.clock.util.extensions.addZero
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StopwatchScreen(onClickSettings: () -> Unit, stopwatchModel: StopwatchModel) {
    val context = LocalContext.current
    val orientation = LocalConfiguration.current.orientation
    val scope = rememberCoroutineScope()
    val timeStampsState = rememberLazyListState()
    TopBarScaffold(title = stringResource(R.string.stopwatch), onClickSettings) { pv ->
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeDisplay(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(2f),
                    innerModifier = Modifier
                        .heightIn(0.dp, 320.dp)
                        .fillMaxWidth(), stopwatchModel
                )
                AnimatedVisibility(stopwatchModel.rememberedTimeStamps.isNotEmpty()) {
                    LapTable(
                        modifier = Modifier
                            .heightIn(0.dp, 300.dp)
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 30.dp), stopwatchModel, timeStampsState
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
                        innerModifier = Modifier
                            .fillMaxSize(),
                        stopwatchModel = stopwatchModel,
                        showProgress = false
                    )
                    StopwatchController(
                        stopwatchModel = stopwatchModel,
                        scope = scope,
                        timeStampsState = timeStampsState,
                        context = context
                    )
                }
                AnimatedVisibility(stopwatchModel.rememberedTimeStamps.isNotEmpty()) {
                    LapTable(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .padding(8.dp),
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
    Row(
        modifier = Modifier
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(stopwatchModel.state == WatchState.RUNNING) {
            Row {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = {
                        stopwatchModel.onLapClicked()
                        scope.launch {
                            timeStampsState.scrollToItem(
                                stopwatchModel.rememberedTimeStamps.size - 1
                            )
                        }
                    }
                ) {
                    Icon(Icons.Default.Timer, null)
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
        LargeFloatingActionButton(
            shape = CircleShape,
            onClick = {
                stopwatchModel.pauseResumeStopwatch(context)
            }
        ) {
            Icon(
                imageVector = if (stopwatchModel.state == WatchState.RUNNING) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = null
            )
        }
        AnimatedVisibility(stopwatchModel.currentPosition != 0L) {
            Row {
                Spacer(modifier = Modifier.width(20.dp))
                if (stopwatchModel.state != WatchState.PAUSED) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { stopwatchModel.stopStopwatch(context) }
                    ) {
                        Icon(Icons.Default.Stop, null)
                    }
                } else {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            stopwatchModel.stopStopwatch(context)
                            stopwatchModel.rememberedTimeStamps.clear()
                        }
                    ) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun LapTable(
    modifier: Modifier = Modifier,
    stopwatchModel: StopwatchModel,
    timeStampsState: LazyListState
) {
    LazyColumn(
        modifier = modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(16.dp),
        state = timeStampsState
    ) {
        item {
            Column {
                Row {
                    Text(
                        text = stringResource(R.string.lap),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.lap_time),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.overall_time),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider()
            }
        }
        itemsIndexed(stopwatchModel.rememberedTimeStamps) { index, time ->
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    String.format("%02d", index + 1),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    time.second.toFullString(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    time.first.toFullString(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

}

@Composable
private fun TimeDisplay(
    modifier: Modifier = Modifier,
    innerModifier: Modifier,
    stopwatchModel: StopwatchModel,
    showProgress: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = innerModifier,
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                val hours = stopwatchModel.currentPosition.div(1000 * 60 * 60)
                val minutes = stopwatchModel.currentPosition.div(1000 * 60).mod(60)
                val seconds =
                    stopwatchModel.currentPosition.div(1000).mod(60)
                val hundreds =
                    stopwatchModel.currentPosition.div(10).mod(100)

                if (hours > 0) {
                    Text(
                        text = hours.toString(),
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(text = ":", style = MaterialTheme.typography.displayLarge)
                }
                Text(
                    text = if (hours > 0) minutes.addZero() else minutes.toString(),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(text = ":", style = MaterialTheme.typography.displayLarge)
                Text(
                    text = seconds.addZero(),
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = hundreds.addZero(),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        if (showProgress) {
            CircularProgressIndicator(
                progress = (stopwatchModel.currentPosition % 60000) / 60000f,
                modifier = Modifier
                    .heightIn(0.dp, 320.dp)
                    .aspectRatio(1f, true),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
        }
    }
}