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
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.BlobIconBox
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.screens.alarm.components.AlarmFilterSection
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel

@Composable
fun AlarmScreen(
    onClickSettings: () -> Unit,
    onAlarm: (alarmId: Long) -> Unit,
    alarmModel: AlarmModel
) {
    val context = LocalContext.current
    val alarms by alarmModel.alarms.collectAsState()
    val filters by alarmModel.filters.collectAsState()

    TopBarScaffold(title = stringResource(R.string.alarm), onClickSettings, fab = {
        if (!alarmModel.showFilter) {
            FloatingActionButton(
                onClick = {
                    onAlarm.invoke(0L)
                }
            ) {
                Icon(Icons.Rounded.Add, null)
            }
        }
    }, actions = {
        Row {
            Box {
                ClickableIcon(
                    imageVector = Icons.Default.Sort
                ) {
                    alarmModel.showSortOrder = !alarmModel.showSortOrder
                }

                DropdownMenu(
                    expanded = alarmModel.showSortOrder,
                    onDismissRequest = { alarmModel.showSortOrder = false }
                ) {
                    AlarmSortOrder.entries.forEach { sortOrder ->
                        DropdownMenuItem(
                            text = { Text(stringResource(sortOrder.value)) },
                            onClick = {
                                alarmModel.setSortOrder(sortOrder)
                                alarmModel.showSortOrder = false
                            }
                        )
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
            ) {
                AlarmItem(
                    alarm = it,
                    onClick = { alarm ->
                        onAlarm.invoke(alarm.id)
                    },
                    onDeleteAlarm = { alarm ->
                        alarmModel.deleteAlarm(alarm)
                    },
                    onUpdateAlarm = { alarm ->
                        alarmModel.updateAlarm(alarm)

                        if (alarm.enabled) {
                            alarmModel.createToast(alarm, context)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
