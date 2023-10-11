package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.common.BlobIconBox
import com.bnyro.clock.ui.components.AlarmRow
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.dialog.TimePickerDialog
import com.bnyro.clock.ui.model.AlarmModel
import com.bnyro.clock.ui.nav.TopBarScaffold

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

    TopBarScaffold(title = stringResource(R.string.alarm), onClickSettings, fab = {
        FloatingActionButton(
            onClick = {
                showCreationDialog = true
            }
        ) {
            Icon(Icons.Default.Create, null)
        }
    }) { pv ->
        if (alarmModel.alarms.isEmpty()) {
            BlobIconBox(icon = R.drawable.ic_alarm)
        }
        LazyColumn(Modifier.fillMaxSize().padding(pv)) {
            items(alarmModel.alarms.sortedBy { it.time }) {
                var showDeletionDialog by remember {
                    mutableStateOf(false)
                }

                val dismissState = rememberDismissState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            DismissValue.DismissedToEnd -> {
                                showDeletionDialog = true
                            }

                            else -> {}
                        }
                        false
                    }
                )
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.StartToEnd),
                    dismissContent = {
                        AlarmRow(it, alarmModel)
                    },
                    background = {}
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
}
