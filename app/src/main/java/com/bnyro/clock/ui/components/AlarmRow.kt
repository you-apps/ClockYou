package com.bnyro.clock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.model.AlarmModel
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmRow(alarm: Alarm, alarmModel: AlarmModel) {
    val context = LocalContext.current

    var showLabelDialog by remember {
        mutableStateOf(false)
    }
    var showPickerDialog by remember {
        mutableStateOf(false)
    }
    var showRingtoneDialog by remember {
        mutableStateOf(false)
    }
    var label by remember {
        mutableStateOf(alarm.label)
    }
    var soundName by remember {
        mutableStateOf(alarm.soundName)
    }

    ElevatedCard(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                showLabelDialog = true
                            }
                            .padding(start = 5.dp, end = 10.dp)
                            .alpha(if (label != null) 1f else 0.5f)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Label, null)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = label ?: stringResource(R.string.label),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = interactionSource
                        ) {
                            showPickerDialog = true
                        },
                        text = TimeHelper.millisToTime(alarm.time).let { time ->
                            "${time.hours.addZero()}:${time.minutes.addZero()}"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 36.sp
                    )
                }

                Column {
                    ClickableIcon(
                        modifier = Modifier.offset(x = 5.dp),
                        imageVector = if (!expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess
                    ) {
                        expanded = !expanded
                    }
                    var isEnabled by remember {
                        mutableStateOf(alarm.enabled)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newValue ->
                            alarm.enabled = newValue
                            isEnabled = newValue
                            alarmModel.updateAlarm(context, alarm)
                        }
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    val chosenDays = remember {
                        alarm.days.toMutableStateList()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AlarmHelper.availableDays.forEachIndexed { index, day ->
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
                                        alarm.days = chosenDays
                                        alarmModel.updateAlarm(context, alarm)
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
                        mutableStateOf(alarm.vibrate)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showRingtoneDialog = true }
                                .padding(vertical = 5.dp, horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(5.dp))
                            val soundTitle = soundName ?: stringResource(R.string.default_sound)
                            Text("${stringResource(R.string.sound)} ($soundTitle)")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.vibrate))
                            Checkbox(
                                checked = vibrationEnabled,
                                onCheckedChange = { newValue ->
                                    alarm.vibrate = newValue
                                    vibrationEnabled = newValue
                                    alarmModel.updateAlarm(context, alarm)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLabelDialog) {
        var newLabel by remember {
            mutableStateOf(label)
        }

        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            title = {
                Text(stringResource(R.string.label))
            },
            text = {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newLabel.orEmpty(),
                    onValueChange = { text -> newLabel = text },
                    singleLine = true
                )
            },
            confirmButton = {
                DialogButton(label = android.R.string.ok) {
                    label = newLabel.takeIf { l -> l.orEmpty().isNotBlank() }
                    alarm.label = label
                    alarmModel.updateAlarm(context, alarm)
                    showLabelDialog = false
                }
            },
            dismissButton = {
                DialogButton(label = android.R.string.cancel) {
                    showLabelDialog = false
                }
            }
        )
    }

    if (showPickerDialog) {
        TimePickerDialog(
            label = stringResource(R.string.edit_alarm),
            initialMillis = alarm.time,
            onDismissRequest = { showPickerDialog = false }
        ) { newValue ->
            alarm.time = newValue.toLong()
            alarmModel.updateAlarm(context, alarm)
            showPickerDialog = false
        }
    }

    if (showRingtoneDialog) {
        RingtonePickerDialog(onDismissRequest = { showRingtoneDialog = false }) { title, uri ->
            alarm.soundUri = uri.toString()
            alarm.soundName = title
            soundName = title
            alarmModel.updateAlarm(context, alarm)
        }
    }
}
