package com.bnyro.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.ui.model.StopwatchModel
import com.bnyro.clock.ui.nav.TopBarScaffold
import kotlinx.coroutines.launch

@Composable
fun StopwatchScreen(onClickSettings: () -> Unit, stopwatchModel: StopwatchModel) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val timeStampsState = rememberLazyListState()
    TopBarScaffold(title = stringResource(R.string.stopwatch), onClickSettings) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(0.dp, 320.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val minutes = stopwatchModel.currentPosition / 60000
                        val seconds =
                            (stopwatchModel.currentPosition % 60000) / 1000
                        val hundreds =
                            stopwatchModel.currentPosition % 1000 / 10

                        Text(
                            text = minutes.toString(),
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
            AnimatedVisibility(stopwatchModel.rememberedTimeStamps.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(0.dp, 300.dp)
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 30.dp),
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
                                    text = stringResource(R.string.lap_times),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.overall_time),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Divider()
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
                AnimatedVisibility(stopwatchModel.currentPosition != 0) {
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
    }
    if (stopwatchModel.state == WatchState.RUNNING) {
        KeepScreenOn()
    }
}

// https://stackoverflow.com/a/71293123/9652621
@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}
