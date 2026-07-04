package com.bnyro.clock.presentation.screens.alarm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.BlobIconBox
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.alarm.components.AlarmFilterSection
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel

@Composable
fun AlarmScreen(
    onClickSettings: () -> Unit,
    onAlarm: (alarmId: Long) -> Unit,
    alarmModel: AlarmModel,
    settingsModel: SettingsModel
) {
    val context = LocalContext.current
    val alarms by alarmModel.alarms.collectAsState()
    val filters by alarmModel.filters.collectAsState()

    val selectedAlarmIds = remember { mutableStateListOf<Long>() }
    val isSelectionMode = selectedAlarmIds.isNotEmpty()

    var wannadeletequestion by remember { mutableStateOf(false) }

    TopBarScaffold(
        title = if (isSelectionMode) {
            "${selectedAlarmIds.size} Selected"
        } else {
            stringResource(R.string.alarm)
        },
        onClickSettings = if (isSelectionMode) {
            { selectedAlarmIds.clear() }
        } else {
            onClickSettings
        },
        fabPosition = settingsModel.fabAlignment.position,
        fab = {
            if (!alarmModel.showFilter && !isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        onAlarm.invoke(0L)
                    }) {
                    Icon(Icons.Rounded.Add, null)
                }
            }
        },
        actions = {
            Row {
                if (isSelectionMode) {
                    ClickableIcon(imageVector = Icons.Default.Delete) {
                        wannadeletequestion = true
                    }
                    ClickableIcon(imageVector = Icons.Default.Close) {
                        selectedAlarmIds.clear()
                    }
                } else {
                    Box {
                        ClickableIcon(
                            imageVector = Icons.AutoMirrored.Filled.Sort
                        ) {
                            alarmModel.showSortOrder = !alarmModel.showSortOrder
                        }

                        DropdownMenu(
                            expanded = alarmModel.showSortOrder,
                            onDismissRequest = { alarmModel.showSortOrder = false }) {
                            AlarmSortOrder.entries.forEach { sortOrder ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(sortOrder.value)) },
                                    onClick = {
                                        alarmModel.setSortOrder(sortOrder)
                                        alarmModel.showSortOrder = false
                                    })
                            }
                        }
                    }

                    ClickableIcon(
                        imageVector = Icons.Default.FilterAlt
                    ) {
                        alarmModel.showFilter = !alarmModel.showFilter
                        if (!alarmModel.showFilter) alarmModel.resetFilters()
                    }
                }
            }
        }) { pv ->

        if (alarms.isEmpty()) {
            BlobIconBox(icon = R.drawable.ic_alarm)
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pv)
        ) {
            item {
                if (alarmModel.showFilter) {
                    AlarmFilterSection(
                        filters,
                        { alarmModel.updateLabelFilter(it) },
                        { alarmModel.updateWeekDayFilter(it) },
                        { alarmModel.updateStartTimeFilter(it) },
                        { alarmModel.updateEndTimeFilter(it) },
                    )
                }
            }

            items(
                items = alarms,
                key = { it.id.toString() + "-" + it.enabled }
            ) { alarm ->
                val isSelected = selectedAlarmIds.contains(alarm.id)

                AlarmItem(
                    alarm = alarm,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onLongClick = { alarmItem ->
                        if (!isSelectionMode) {
                            selectedAlarmIds.add(alarmItem.id)
                        }
                    },
                    onClick = { alarmItem ->
                        if (isSelectionMode) {
                            if (isSelected) {
                                selectedAlarmIds.remove(alarmItem.id)
                            } else {
                                selectedAlarmIds.add(alarmItem.id)
                            }
                        } else {
                            onAlarm.invoke(alarmItem.id)
                        }
                    },
                    onDeleteAlarm = { alarmItem ->
                        alarmModel.deleteAlarm(alarmItem)
                    },
                    onUpdateAlarm = { updatedAlarm ->
                        if (!isSelectionMode) {
                            alarmModel.updateAlarm(updatedAlarm)

                            if (updatedAlarm.enabled) {
                                alarmModel.createToast(updatedAlarm, context)
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }


        if (wannadeletequestion) {
            AlertDialog(
                onDismissRequest = { wannadeletequestion = false },
                title = {
                    Text(text = stringResource(R.string.delete_alarms))
                },
                text = {
                    Text(text = stringResource(R.string.irreversible))
                },
                confirmButton = {
                    DialogButton(label = android.R.string.ok) {
                        alarms.filter { selectedAlarmIds.contains(it.id) }.forEach { alarm ->
                            alarmModel.deleteAlarm(alarm)
                        }
                        selectedAlarmIds.clear()
                        wannadeletequestion = false
                    }
                },
                dismissButton = {
                    DialogButton(label = android.R.string.cancel) {
                        wannadeletequestion = false
                    }
                }
            )
        }
    }
}