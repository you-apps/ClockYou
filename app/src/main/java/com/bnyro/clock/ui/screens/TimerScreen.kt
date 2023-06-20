package com.bnyro.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.ui.components.FormattedTimerTime
import com.bnyro.clock.ui.components.NumberKeypad
import com.bnyro.clock.ui.components.Operation
import com.bnyro.clock.ui.model.TimerModel

@Composable
fun TimerScreen(timerModel: TimerModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        timerModel.tryConnect(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (timerModel.state == WatchState.IDLE) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FormattedTimerTime(
                        seconds = timerModel.secondsState.value.toInt(),
                        modifier = Modifier.padding(bottom = 30.dp),
                    )
                    NumberKeypad(
                        onOperation = { operation ->
                            when (operation) {
                                is Operation.AddNumber -> timerModel.addNumber(operation.number)
                                is Operation.Delete -> timerModel.deleteLastNumber()
                            }
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val minutes = timerModel.currentTimeMillis / 60000
                        val seconds = (timerModel.currentTimeMillis % 60000) / 1000
                        val hundreds = timerModel.currentTimeMillis % 1000 / 10

                        Text(
                            text = minutes.toString(),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = seconds.toString(),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = hundreds.toString()
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = {
                    when (timerModel.state) {
                        WatchState.PAUSED -> timerModel.resumeTimer()
                        WatchState.RUNNING -> timerModel.pauseTimer()
                        else -> timerModel.startTimer(context)
                    }
                }
            ) {
                Icon(
                    imageVector = if (timerModel.state == WatchState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            AnimatedVisibility(timerModel.state != WatchState.IDLE) {
                Row {
                    Spacer(modifier = Modifier.width(20.dp))
                    FloatingActionButton(
                        onClick = { timerModel.stopTimer(context) }
                    ) {
                        Icon(Icons.Default.Stop, null)
                    }
                }
            }
        }
    }
}
