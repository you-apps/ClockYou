package com.bnyro.clock.presentation.screens.alarmpicker.components

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.components.SwitchItem
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.features.RingtonePickerDialog
import com.bnyro.clock.presentation.features.VibrationPatternPickerDialog
import com.bnyro.clock.presentation.screens.alarm.components.AlarmTimePicker
import com.bnyro.clock.presentation.screens.alarm.components.SnoozeTimePickerDialog
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmPicker(currentAlarm: Alarm, onSave: (Alarm) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf(currentAlarm.label ?: "") }
    val chosenDays = remember { currentAlarm.days.toMutableStateList() }
    var vibrationEnabled by remember {
        mutableStateOf(currentAlarm.vibrate)
    }
    var vibrationPattern by remember {
        mutableStateOf(currentAlarm.vibrationPattern)
    }
    var vibrationPatternName by remember {
        mutableStateOf(currentAlarm.vibrationPatternName)
    }
    var soundName by remember { mutableStateOf(currentAlarm.soundName) }
    var soundUri by remember { mutableStateOf(currentAlarm.soundUri) }
    var repeat by remember { mutableStateOf(currentAlarm.repeat) }
    var snoozeMinutes by remember { mutableIntStateOf(currentAlarm.snoozeMinutes) }
    var snoozeEnabled by remember { mutableStateOf(currentAlarm.snoozeEnabled) }
    var soundEnabled by remember { mutableStateOf(currentAlarm.soundEnabled) }

    val initialTime = remember { TimeHelper.millisToTime(currentAlarm.time) }
    var hours by remember { mutableIntStateOf(initialTime.hours) }
    var minutes by remember { mutableIntStateOf(initialTime.minutes) }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            AlarmTimePicker(
                hours,
                minutes,
                onHoursChanged = { hours = it },
                onMinutesChanged = { minutes = it }
            )
            Column {
                SwitchItem(
                    title = stringResource(R.string.repeat),
                    isChecked = repeat,
                    onClick = { newValue ->
                        repeat = newValue
                    },
                    icon = Icons.Rounded.EventRepeat
                )
                AnimatedVisibility(visible = repeat) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = remember {
                            AlarmHelper.getDaysOfWeekByLocale(context)
                        }

                        daysOfWeek.forEach { (day, index) ->
                            val enabled = chosenDays.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        if (enabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .border(
                                        if (enabled) 0.dp else 1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .clickable {
                                        if (enabled) {
                                            if (chosenDays.size > 1) chosenDays.remove(index)
                                        } else {
                                            chosenDays.add(
                                                index
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(8.dp, 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = label,
                        onValueChange = {
                            label = it
                        },
                        label = {
                            Text(text = stringResource(id = R.string.alarm_name))
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Label, contentDescription = null)
                        }
                    )
                }
                SwitchWithDivider(
                    title = stringResource(R.string.sound),
                    description = soundName ?: stringResource(R.string.default_sound),
                    isChecked = soundEnabled,
                    icon = Icons.Rounded.Alarm,
                    onClick = {
                        showRingtoneDialog = true
                    },
                    onChecked = {
                        soundEnabled = it
                    }
                )
                SwitchWithDivider(
                    title = stringResource(R.string.vibrate),
                    description = stringResource(
                        id = R.string.vibration_pattern,
                        vibrationPatternName
                    ),
                    isChecked = vibrationEnabled,
                    icon = Icons.Rounded.Vibration,
                    onClick = {
                        showVibrationDialog = true
                    },
                    onChecked = { newValue ->
                        vibrationEnabled = newValue
                    }
                )
                SwitchWithDivider(
                    title = stringResource(R.string.snooze),
                    description = with(snoozeMinutes) {
                        pluralStringResource(
                            id = R.plurals.minutes,
                            count = this,
                            this
                        )
                    },
                    isChecked = snoozeEnabled,
                    icon = Icons.Rounded.Snooze,
                    onClick = {
                        showSnoozeDialog = true
                    },
                    onChecked = {
                        snoozeEnabled = it
                    }
                )
            }
        }

        Row(
            Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = { onCancel.invoke() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Button(onClick = {
                val alarm =
                    currentAlarm.copy(
                        time = (hours * 60 + minutes) * 60 * 1000L,
                        label = label.takeIf { l -> l.isNotBlank() },
                        days = chosenDays.sorted(),
                        vibrate = vibrationEnabled,
                        soundName = soundName,
                        soundUri = soundUri,
                        repeat = repeat,
                        snoozeEnabled = snoozeEnabled,
                        snoozeMinutes = snoozeMinutes,
                        soundEnabled = soundEnabled,
                        vibrationPattern = vibrationPattern,
                        vibrationPatternName = vibrationPatternName
                    )
                onSave(alarm)
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }

        // extra spacing to fix that the buttons are overlapped by the navigation bar
        if (!isGestureNavigationMode(context.contentResolver)) {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    if (showRingtoneDialog) {
        RingtonePickerDialog(onDismissRequest = {
            showRingtoneDialog = false
        }) { title, uri ->
            soundUri = uri.toString()
            soundName = title
        }
    }
    if (showSnoozeDialog) {
        SnoozeTimePickerDialog(
            onDismissRequest = { showSnoozeDialog = false },
            currentTime = snoozeMinutes,
            onTimeSet = {
                snoozeMinutes = it
                showSnoozeDialog = false
            }
        )
    }
    if (showVibrationDialog) {
        VibrationPatternPickerDialog(
            onDismissRequest = { showVibrationDialog = false },
            onSelectPattern = {
                vibrationPattern = it.pattern
                vibrationPatternName = it.name
                showVibrationDialog = false
            },
            selectedPattern = vibrationPatternName
        )
    }
}

fun isGestureNavigationMode(content: ContentResolver?): Boolean {
    return Settings.Secure.getInt(content, "navigation_mode", 0) == 2
}