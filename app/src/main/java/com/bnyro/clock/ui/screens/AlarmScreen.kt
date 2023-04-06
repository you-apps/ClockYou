package com.bnyro.clock.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.components.TimePickerDialog
import com.bnyro.clock.ui.model.AlarmModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(alarmModel: AlarmModel) {
    val context = LocalContext.current
    var showCreationDialog by remember {
        mutableStateOf(false)
    }
    val availableDays = listOf("S", "M", "T", "W", "T", "F", "S")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(alarmModel.alarms) {
                var showDeletionDialog by remember {
                    mutableStateOf(false)
                }
                var showPickerDialog by remember {
                    mutableStateOf(false)
                }

                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        when (it) {
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
                        ElevatedCard(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            var expanded by remember {
                                mutableStateOf(false)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    Text(
                                        modifier = Modifier.clickable(
                                            indication = null,
                                            interactionSource = interactionSource
                                        ) {
                                            showPickerDialog = true
                                        },
                                        text = DateUtils.formatElapsedTime(it.time / 1000)
                                            .toString()
                                            .replace(":00$".toRegex(), ""),
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Column {
                                        ClickableIcon(
                                            imageVector = if (!expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess
                                        ) {
                                            expanded = !expanded
                                        }
                                        var isEnabled by remember {
                                            mutableStateOf(it.enabled)
                                        }
                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { newValue ->
                                                it.enabled = newValue
                                                isEnabled = newValue
                                                alarmModel.updateAlarm(context, it)
                                            }
                                        )
                                    }
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Column {
                                        val chosenDays = remember {
                                            it.days.toMutableStateList()
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 15.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            availableDays.forEachIndexed { index, day ->
                                                val enabled = chosenDays.contains(index)
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .background(
                                                            if (enabled) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                                            CircleShape
                                                        )
                                                        .clip(CircleShape)
                                                        .border(
                                                            if (enabled) 0.dp else 1.dp,
                                                            MaterialTheme.colorScheme.outline,
                                                            CircleShape
                                                        )
                                                        .clickable {
                                                            if (enabled) {
                                                                chosenDays.remove(index)
                                                            } else {
                                                                chosenDays.add(
                                                                    index
                                                                )
                                                            }
                                                            it.days = chosenDays
                                                            alarmModel.updateAlarm(context, it)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = day,
                                                        color = if (enabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                        }
                                        var vibrationEnabled by remember {
                                            mutableStateOf(it.vibrate)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.vibrate))
                                            Checkbox(
                                                checked = vibrationEnabled,
                                                onCheckedChange = { newValue ->
                                                    it.vibrate = newValue
                                                    vibrationEnabled = newValue
                                                    alarmModel.updateAlarm(context, it)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
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

                if (showPickerDialog) {
                    TimePickerDialog(
                        label = stringResource(R.string.edit_alarm),
                        initialMillis = it.time,
                        onDismissRequest = { showPickerDialog = false }
                    ) { newValue ->
                        it.time = newValue.toLong()
                        alarmModel.updateAlarm(context, it)
                        showPickerDialog = false
                    }
                }
            }
        }
        if (alarmModel.alarms.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(150.dp),
                    imageVector = Icons.Default.Layers,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.nothing_here),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                showCreationDialog = true
            }
        ) {
            Icon(Icons.Default.Create, null)
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
