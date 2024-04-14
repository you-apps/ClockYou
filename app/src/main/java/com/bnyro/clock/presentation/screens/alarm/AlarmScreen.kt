package com.bnyro.clock.presentation.screens.alarm

import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.BlobIconBox
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.alarm.components.AlarmFilterSection
import com.bnyro.clock.presentation.screens.alarm.components.AlarmRow
import com.bnyro.clock.presentation.screens.alarm.components.AlarmSettingsSheet
import com.bnyro.clock.presentation.screens.alarm.components.TimePickerDialog
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.util.AlarmHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onClickSettings: () -> Unit,
    alarmModel: AlarmModel
) {
    val context = LocalContext.current
    var showCreationDialog by remember {
        mutableStateOf(false)
    }
    val alarms by alarmModel.alarms.collectAsState()
    val filters by alarmModel.filters.collectAsState()

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            if (!AlarmHelper.hasPermission(context)) {
                val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${BuildConfig.APPLICATION_ID}".toUri()
                }
                context.startActivity(intent)
            }
        }
    }

    TopBarScaffold(title = stringResource(R.string.alarm), onClickSettings, fab = {
        if (!alarmModel.showFilter) {
            FloatingActionButton(
                onClick = {
                    showCreationDialog = true
                }
            ) {
                Icon(Icons.Default.Create, null)
            }
        }
    }, actions = {
        ClickableIcon(
            imageVector = Icons.Default.FilterAlt
        ) {
            alarmModel.showFilter = !alarmModel.showFilter
            if (!alarmModel.showFilter) alarmModel.resetFilters()
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

            items(items = alarms, key = { it.id }) {
                var showDeletionDialog by remember {
                    mutableStateOf(false)
                }

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                showDeletionDialog = true
                            }

                            else -> {}
                        }
                        false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    content = {
                        AlarmRow(it, alarmModel)
                    },
                    backgroundContent = {

                    }
                )

                if (showDeletionDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreationDialog = false },
                        title = {
                            Text(text = stringResource(R.string.delete_alarm))
                        },
                        text = {
                            Text(text = stringResource(R.string.irreversible))
                        },
                        confirmButton = {
                            DialogButton(label = android.R.string.ok) {
                                alarmModel.deleteAlarm(context, it)
                                showDeletionDialog = false
                            }
                        },
                        dismissButton = {
                            DialogButton(label = android.R.string.cancel) {
                                showDeletionDialog = false
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showCreationDialog) {
            TimePickerDialog(
                label = stringResource(R.string.new_alarm),
                onDismissRequest = { showCreationDialog = false }
            ) {
                val alarm = Alarm(time = it.toLong())
                alarmModel.createAlarm(alarm)
                showCreationDialog = false
            }
        }
    }
    alarmModel.selectedAlarm?.let {
        AlarmSettingsSheet(
            onDismissRequest = { alarmModel.selectedAlarm = null },
            currentAlarm = it,
            onSave = { newAlarm ->
                alarmModel.updateAlarm(context, newAlarm)
            }
        )
    }
}
