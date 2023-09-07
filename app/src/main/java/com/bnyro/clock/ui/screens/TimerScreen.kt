package com.bnyro.clock.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.bnyro.clock.obj.NumberKeypadOperation
import com.bnyro.clock.ui.components.FormattedTimerTime
import com.bnyro.clock.ui.components.NumberKeypad
import com.bnyro.clock.ui.components.TimePickerDial
import com.bnyro.clock.ui.components.TimerItem
import com.bnyro.clock.ui.model.TimerModel
import com.bnyro.clock.util.Preferences

@OptIn(ExperimentalFoundationApi::class)
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

    Scaffold(floatingActionButton = {
        if (timerModel.scheduledObjects.isEmpty() || createNew) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (timerModel.scheduledObjects.isNotEmpty()) {
                    SmallFloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { createNew = false }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                FloatingActionButton(onClick = {
                    timerModel.addPersistentTimer(timerModel.timePickerSeconds)
                }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                }
                Spacer(Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = {
                        createNew = false
                        timerModel.startTimer(context)
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        } else {
            FloatingActionButton(
                modifier = Modifier,
                onClick = { createNew = true }
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = null)
            }
        }
    }) { paddingValues ->
        if (timerModel.scheduledObjects.isEmpty() || createNew) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (useOldPicker) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .weight(2f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePickerDial(timerModel)
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(3f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FormattedTimerTime(
                            seconds = timerModel.timePickerFakeUnits,
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
                if (showExampleTimers) {
                    val haptic = LocalHapticFeedback.current
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items = timerModel.persistentTimers) { index, timer ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = {
                                            timerModel.timePickerSeconds = timer.seconds
                                            createNew = false
                                            timerModel.startTimer(context)
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            timerModel.removePersistentTimer(index)
                                        }
                                    )
                                    .width(100.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    timer.formattedTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top
            ) {
                itemsIndexed(timerModel.scheduledObjects) { index, obj ->
                    TimerItem(obj, index, timerModel)
                }
            }
        }
    }
}
