package com.bnyro.clock.presentation.screens.alarmpicker.components

import android.content.ContentResolver
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.presentation.components.ClockTimePicker
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.features.RingtonePickerDialog
import com.bnyro.clock.presentation.features.VibrationPatternPickerDialog
import com.bnyro.clock.presentation.screens.alarm.components.AlarmTimePicker
import com.bnyro.clock.presentation.screens.alarm.components.ScrollAlarmTimePicker
import com.bnyro.clock.presentation.screens.alarm.components.MinutePickerDialog
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmPicker(
    currentAlarm: Alarm,
    onSave: (Alarm) -> Unit,
    onDelete: ((Alarm) -> Unit)? = null,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf(currentAlarm.label ?: "") }

    val isNewAlarm = currentAlarm.id == 0L
    val chosenDays = remember {
        (if (isNewAlarm) listOf(0, 1, 2, 3, 4, 5, 6) else currentAlarm.days).toMutableStateList()
    }
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

    var startDate by remember { mutableLongStateOf(currentAlarm.startDate) }
    var repeatDuration by remember { mutableStateOf(currentAlarm.repeatDuration) }
    var repeatDurationUnit by remember { mutableStateOf(currentAlarm.repeatDurationUnit) }
    var repeatInterval by remember { mutableIntStateOf(currentAlarm.repeatInterval) }
    var repeatUnit by remember { mutableStateOf(currentAlarm.repeatUnit) }
    var repeatAnchor by remember { mutableStateOf(currentAlarm.repeatAnchor) }
    var endDate by remember { mutableStateOf(currentAlarm.endDate) }
    var endOccurrences by remember { mutableStateOf(currentAlarm.endOccurrences) }

    var snoozeMinutes by remember { mutableIntStateOf(currentAlarm.snoozeMinutes) }
    var snoozeEnabled by remember { mutableStateOf(currentAlarm.snoozeEnabled) }
    var soundEnabled by remember { mutableStateOf(currentAlarm.soundEnabled) }

    val initialTime = remember { TimeHelper.millisToTime(currentAlarm.time) }
    var hours by remember { mutableIntStateOf(initialTime.hours) }
    var minutes by remember { mutableIntStateOf(initialTime.minutes) }

    val scrollState = rememberScrollState()

    val pickerStyle = remember {
        PickerStyle.valueOf(
            Preferences.instance.getString(
                Preferences.alarmPickerStyleKey,
                PickerStyle.WHEEL.name
            ) ?: PickerStyle.WHEEL.name
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
            when (pickerStyle) {
                PickerStyle.WHEEL -> ScrollAlarmTimePicker(
                    initialHours = hours,
                    initialMinutes = minutes,
                    onHoursChanged = { hours = it },
                    onMinutesChanged = { minutes = it }
                )

                PickerStyle.NUMBER_PAD -> AlarmTimePicker(
                    initialHours = hours,
                    initialMinutes = minutes,
                    isEditing = currentAlarm.id != 0L,
                    onHoursChanged = { hours = it },
                    onMinutesChanged = { minutes = it }
                )

                PickerStyle.CLOCK -> ClockTimePicker(
                    initialHours = hours,
                    initialMinutes = minutes,
                    is24Hour = DateFormat.is24HourFormat(context),
                    onHoursChanged = { hours = it },
                    onMinutesChanged = { minutes = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column {
                RecurrencePicker(
                    startDate = startDate,
                    repeatDuration = repeatDuration,
                    repeatDurationUnit = repeatDurationUnit,
                    repeatInterval = repeatInterval,
                    repeatUnit = repeatUnit,
                    repeatAnchor = repeatAnchor,
                    chosenDays = chosenDays,
                    endDate = endDate,
                    endOccurrences = endOccurrences,
                    onStartDateChange = { startDate = it },
                    onRepeatDurationChange = { duration, unit ->
                        repeatDuration = duration
                        repeatDurationUnit = unit
                    },
                    onRepeatIntervalChange = { repeatInterval = it },
                    onRepeatUnitChange = { repeatUnit = it },
                    onRepeatAnchorChange = { repeatAnchor = it },
                    onEndChange = { newEndDate, newEndOccurrences ->
                        endDate = newEndDate
                        endOccurrences = newEndOccurrences
                    }
                )
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
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.Label, contentDescription = null)
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isNewAlarm && onDelete != null) {
                FilledTonalButton(
                    onClick = { onDelete(currentAlarm) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { onCancel.invoke() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                val alarm =
                    currentAlarm.copy(
                        time = (hours * 60 + minutes) * 60 * 1000L,
                        label = label.takeIf { l -> l.isNotBlank() },
                        days = chosenDays.sorted(),
                        vibrate = vibrationEnabled,
                        soundName = soundName,
                        soundUri = soundUri,
                        snoozeEnabled = snoozeEnabled,
                        snoozeMinutes = snoozeMinutes,
                        soundEnabled = soundEnabled,
                        vibrationPattern = vibrationPattern,
                        vibrationPatternName = vibrationPatternName,
                        startDate = startDate,
                        repeatDuration = repeatDuration,
                        repeatDurationUnit = repeatDurationUnit,
                        repeatInterval = repeatInterval,
                        repeatUnit = repeatUnit,
                        repeatAnchor = repeatAnchor,
                        endDate = endDate,
                        endOccurrences = endOccurrences
                    )
                onSave(alarm)
            }) {
                Text(text = stringResource(R.string.save))
            }
        }

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
        MinutePickerDialog(
            onDismissRequest = { showSnoozeDialog = false },
            currentTime = snoozeMinutes,
            title = R.string.select_snooze_time,
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

// extra spacing to fix that the buttons are overlapped by the navigation bar
fun isGestureNavigationMode(content: ContentResolver?): Boolean {
    return Settings.Secure.getInt(content, "navigation_mode", 0) == 2
}
