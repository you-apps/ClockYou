package com.bnyro.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.ui.model.StopwatchModel
import com.bnyro.clock.ui.nav.TopBarScaffold
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.launch

@Composable
fun StopwatchScreen(onClickSettings: () -> Unit, stopwatchModel: StopwatchModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        stopwatchModel.tryConnect(context)
    }

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
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .border(8.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val minutes = stopwatchModel.scheduledObject.currentPosition.value / 60000
                        val seconds =
                            (stopwatchModel.scheduledObject.currentPosition.value % 60000) / 1000
                        val hundreds =
                            stopwatchModel.scheduledObject.currentPosition.value % 1000 / 10

                        Text(
                            text = minutes.toString(),
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
            }
            AnimatedVisibility(stopwatchModel.rememberedTimeStamps.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .height(100.dp)
                        .padding(bottom = 30.dp),
                    state = timeStampsState
                ) {
                    itemsIndexed(stopwatchModel.rememberedTimeStamps) { index, timeStamp ->
                        val time = TimeHelper.millisToTime(timeStamp.toLong())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("#${index + 1}")
                            Text(time.toString())
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
                AnimatedVisibility(stopwatchModel.scheduledObject.state.value == WatchState.RUNNING) {
                    Row {
                        FloatingActionButton(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = {
                                stopwatchModel.rememberedTimeStamps.add(
                                    stopwatchModel.scheduledObject.currentPosition.value
                                )
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
                        when (stopwatchModel.scheduledObject.state.value) {
                            WatchState.PAUSED -> stopwatchModel.resumeStopwatch()
                            WatchState.RUNNING -> stopwatchModel.pauseStopwatch()
                            else -> stopwatchModel.startStopwatch(context)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (stopwatchModel.scheduledObject.state.value == WatchState.RUNNING) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = null
                    )
                }
                AnimatedVisibility(stopwatchModel.scheduledObject.currentPosition.value != 0) {
                    Row {
                        Spacer(modifier = Modifier.width(20.dp))
                        if (stopwatchModel.scheduledObject.state.value != WatchState.PAUSED) {
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
                                    stopwatchModel.scheduledObject.currentPosition.value = 0
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
    if (stopwatchModel.scheduledObject.state.value == WatchState.RUNNING)
        KeepScreenOn()
}

//https://stackoverflow.com/a/71293123/9652621
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