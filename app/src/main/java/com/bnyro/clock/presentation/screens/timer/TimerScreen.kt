package com.bnyro.clock.presentation.screens.timer

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAlarm
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.NumberKeypadOperation
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.TimePickerDial
import com.bnyro.clock.presentation.screens.timer.components.FormattedTimerTime
import com.bnyro.clock.presentation.screens.timer.components.NumberKeypad
import com.bnyro.clock.presentation.screens.timer.components.TimerItem
import com.bnyro.clock.presentation.screens.timer.model.TimerModel
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.extensions.KeepScreenOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onClickSettings: () -> Unit, timerModel: TimerModel) {
    val context = LocalContext.current
    val useScrollPicker = Preferences.instance.getBoolean(Preferences.timerUsePickerKey, false)
    val showExampleTimers = Preferences.instance.getBoolean(Preferences.timerShowExamplesKey, true)

    var createNew by remember {
        mutableStateOf(false)
    }

    val scheduledObjects by timerModel.scheduledObjects.collectAsState()

    TopBarScaffold(title = stringResource(R.string.timer), onClickSettings, actions = {
        if (scheduledObjects.isEmpty()) {
            ClickableIcon(
                imageVector = Icons.Rounded.AddAlarm,
                contentDescription = stringResource(R.string.add_preset_timer)
            ) {
                timerModel.addPersistentTimer(timerModel.timePickerSeconds)
            }
        } else {
            ClickableIcon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_preset_timer)
            ) {
                createNew = true
            }
        }
    }) { paddingValues ->
        if (scheduledObjects.isEmpty()) {
            Column(
                Modifier
                    .padding(paddingValues)
            ) {
                TimerPicker(
                    useScrollPicker,
                    timerModel,
                    showExampleTimers,
                    context,
                    onCreateNew = {
                        createNew = false
                    },
                    showFAB = true
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top
            ) {
                items(scheduledObjects, key = { it.id }) { obj ->
                    TimerItem(obj, timerModel)
                }
            }
            KeepScreenOn()
        }
    }

    if (createNew) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { createNew = false },
            sheetState = sheetState
        ) {
            TimerPicker(
                useScrollPicker,
                timerModel,
                showExampleTimers,
                context,
                onCreateNew = {
                    createNew = false
                },
                showFAB = false
            )
        }
    }
}

@Composable
private fun TimerPicker(
    useScrollPicker: Boolean,
    timerModel: TimerModel,
    showExampleTimers: Boolean,
    context: Context,
    onCreateNew: () -> Unit,
    showFAB: Boolean
) {
    val orientation = LocalConfiguration.current.orientation
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.weight(1f)) {
                TimerPickerSelector(useScrollPicker, timerModel)
            }
            if (showExampleTimers) {
                PresetTimers(timerModel, onCreateNew, context)
            }
            StartTimerButton(showFAB, onCreateNew, timerModel, context)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                TimerPickerSelector(useScrollPicker, timerModel)
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showExampleTimers) {
                    PresetTimers(timerModel, onCreateNew, context)
                }
                StartTimerButton(showFAB = false, onCreateNew, timerModel, context)
            }
        }
    }
}

@Composable
private fun ColumnScope.StartTimerButton(
    showFAB: Boolean,
    onCreateNew: () -> Unit,
    timerModel: TimerModel,
    context: Context
) {
    if (showFAB) {
        FloatingActionButton(
            modifier = Modifier.Companion
                .align(Alignment.End)
                .padding(vertical = 16.dp)
                .padding(end = 16.dp),
            onClick = {
                onCreateNew.invoke()
                timerModel.startTimer(context)
            }) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.start)
            )
        }
    } else {
        Button(modifier = Modifier.padding(vertical = 16.dp), onClick = {
            onCreateNew.invoke()
            timerModel.startTimer(context)
        }) {
            Text(
                text = stringResource(R.string.start),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PresetTimers(
    timerModel: TimerModel,
    onCreateNew: () -> Unit,
    context: Context
) {
    val haptic = LocalHapticFeedback.current
    LazyVerticalGrid(
        modifier = Modifier
            .heightIn(0.dp, 200.dp)
            .fillMaxWidth(),
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
                            onCreateNew.invoke()
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

@Composable
private fun TimerPickerSelector(
    useScrollPicker: Boolean,
    timerModel: TimerModel
) {
    if (!useScrollPicker) {
        Row(
            Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimePickerDial(timerModel)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
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
}
