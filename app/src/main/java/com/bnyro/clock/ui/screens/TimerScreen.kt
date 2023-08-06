package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.NumberKeypadOperation
import com.bnyro.clock.obj.WatchState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SmallFloatingActionButton
import com.bnyro.clock.ui.common.ExampleTimer
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.components.FormattedTimerTime
import com.bnyro.clock.ui.components.NumberKeypad
import com.bnyro.clock.ui.components.NumberPicker
import com.bnyro.clock.ui.components.RingtonePickerDialog
import com.bnyro.clock.ui.model.TimerModel
import com.bnyro.clock.util.Preferences

@Composable
fun TimerScreen(timerModel: TimerModel) {
    val context = LocalContext.current
    val useOldPicker = Preferences.instance.getBoolean(Preferences.timerUsePickerKey, false)
    val showExampleTimers = Preferences.instance.getBoolean(Preferences.timerShowExamplesKey, true)

    LaunchedEffect(Unit) {
        timerModel.tryConnect(context)
    }
    var createNew by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (timerModel.scheduledObjects.isEmpty() || createNew) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (useOldPicker) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberPicker(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp, end = 8.dp),
                            textStyle = MaterialTheme.typography.displayLarge,
                            value = timerModel.getHours(),
                            onValueChanged = timerModel::addHours,
                            range = 0..24
                        )
                        NumberPicker(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            textStyle = MaterialTheme.typography.displayLarge,
                            value = timerModel.getMinutes(),
                            onValueChanged = timerModel::addMinutes,
                            range = 0..60
                        )
                        NumberPicker(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp, end = 24.dp),
                            textStyle = MaterialTheme.typography.displayLarge,
                            value = timerModel.getSeconds(),
                            onValueChanged = timerModel::addSeconds,
                            range = 0..60
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FormattedTimerTime(
                            seconds = timerModel.timePickerSecondsState.toInt(),
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                        NumberKeypad(
                            onOperation = { operation ->
                                when (operation) {
                                    is NumberKeypadOperation.AddNumber -> timerModel.addNumber(
                                        operation.number
                                    )

                                    is NumberKeypadOperation.Delete -> timerModel.deleteLastNumber()
                                    is NumberKeypadOperation.Clear -> timerModel.clear()
                                }
                            }
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    if (timerModel.scheduledObjects.isNotEmpty()) {
                        SmallFloatingActionButton(
                            modifier = Modifier.padding(end = 16.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = { createNew = false }
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LazyRow(
                            contentPadding = PaddingValues(start = 40.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (showExampleTimers) {
                                items(ExampleTimer.exampleTimers) { timer ->
                                    Button(
                                        onClick = {
                                            timerModel.setSeconds(timer.seconds)
                                            createNew = false
                                            timerModel.startTimer(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(),
                                    ) {
                                        Text(
                                            timer.formattedTime,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = {
                            createNew = false
                            timerModel.startTimer(context)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                itemsIndexed(timerModel.scheduledObjects) { index, obj ->
                    val minutes = obj.currentPosition.value / 60000
                    val seconds = (obj.currentPosition.value % 60000) / 1000
                    val hundreds = obj.currentPosition.value % 1000 / 10

                    var showLabelEditor by remember {
                        mutableStateOf(false)
                    }
                    var showRingtoneEditor by remember {
                        mutableStateOf(false)
                    }

                    ElevatedCard(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 16.dp
                            )
                        ) {
                            Column {
                                obj.label.value?.let { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.headlineLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "$minutes:${seconds.addZero()}",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = hundreds.addZero()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            ClickableIcon(imageVector = Icons.Default.Edit) {
                                showLabelEditor = true
                            }
                            ClickableIcon(imageVector = Icons.Default.Notifications) {
                                showRingtoneEditor = true
                            }
                            ClickableIcon(
                                imageVector = if (obj.state.value == WatchState.RUNNING) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                }
                            ) {
                                when (obj.state.value) {
                                    WatchState.PAUSED -> timerModel.resumeTimer(index)
                                    WatchState.RUNNING -> timerModel.pauseTimer(index)
                                    else -> timerModel.startTimer(context)
                                }
                            }
                            ClickableIcon(imageVector = Icons.Default.Stop) {
                                timerModel.stopTimer(context, index)
                            }
                        }
                    }

                    if (showLabelEditor) {
                        var newLabel by remember {
                            mutableStateOf(obj.label.value.orEmpty())
                        }

                        AlertDialog(
                            onDismissRequest = { showLabelEditor = false },
                            confirmButton = {
                                DialogButton(android.R.string.ok) {
                                    timerModel.service?.updateLabel(obj.id, newLabel)
                                    newLabel = ""
                                    showLabelEditor = false
                                }
                            },
                            dismissButton = {
                                DialogButton(android.R.string.cancel) {
                                    newLabel = ""
                                    showLabelEditor = false
                                }
                            },
                            title = {
                                Text(stringResource(R.string.label))
                            },
                            text = {
                                OutlinedTextField(
                                    value = newLabel,
                                    onValueChange = { newLabel = it },
                                    label = {
                                        Text(stringResource(R.string.label))
                                    }
                                )
                            }
                        )
                    }

                    if (showRingtoneEditor) {
                        RingtonePickerDialog(onDismissRequest = { showRingtoneEditor = false }) { _, uri ->
                            timerModel.service?.updateRingtone(obj.id, uri)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { createNew = true }
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = null)
            }
        }
    }
}
