package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.presentation.components.ScrollPickerDialog
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.features.RingtonePickerDialog
import com.bnyro.clock.presentation.features.VibrationPatternPickerDialog
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerEditSheet(
    currentTimer: TimerSettings,
    pickerStyle: PickerStyle,
    onSave: (TimerSettings) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }
    var showIncrementDialog by remember { mutableStateOf(false) }

    var seconds by remember { mutableIntStateOf(currentTimer.seconds) }
    var label by remember { mutableStateOf(currentTimer.label) }
    var soundName by remember { mutableStateOf(currentTimer.soundName) }
    var soundUri by remember { mutableStateOf(currentTimer.soundUri) }
    var soundEnabled by remember { mutableStateOf(currentTimer.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(currentTimer.vibrate) }
    var vibrationPattern by remember { mutableStateOf(currentTimer.vibrationPattern) }
    var vibrationPatternName by remember { mutableStateOf(currentTimer.vibrationPatternName) }
    var incrementSeconds by remember { mutableStateOf(currentTimer.incrementSeconds) }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerPickerSelector(
                    pickerStyle = pickerStyle,
                    seconds = seconds,
                    onSecondsChanged = { seconds = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column {
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
                                Text(text = stringResource(id = R.string.label))
                            },
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Label,
                                    contentDescription = null
                                )
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIncrementDialog = true }
                            .padding(8.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreTime,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 16.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.timer_increment),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = incrementSeconds?.let {
                                    pluralStringResource(R.plurals.seconds_count, it, it)
                                } ?: stringResource(R.string.default_increment),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onDelete?.let {
                    FilledTonalButton(
                        onClick = it,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onDismiss) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    enabled = seconds > 0,
                    onClick = {
                        onSave(
                            currentTimer.copy(
                                seconds = seconds,
                                // a timer without a name of its own is named by its duration
                                label = label.ifBlank { TimeHelper.durationToName(seconds) },
                                soundName = soundName,
                                soundUri = soundUri,
                                soundEnabled = soundEnabled,
                                vibrate = vibrationEnabled,
                                vibrationPattern = vibrationPattern,
                                vibrationPatternName = vibrationPatternName,
                                incrementSeconds = incrementSeconds
                            )
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    }
    if (showRingtoneDialog) {
        RingtonePickerDialog(onDismissRequest = {
            showRingtoneDialog = false
        }) { title, uri ->
            soundUri = uri?.toString()
            soundName = title
        }
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
    if (showIncrementDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showIncrementDialog = false },
            title = stringResource(R.string.select_timer_increment),
            unit = stringResource(R.string.seconds),
            value = incrementSeconds
                ?: Preferences.instance.getInt(Preferences.timerIncrementSecondsKey, 60),
            maxValue = 61,
            offset = 0,
            label = { it.toString() },
            onValueSet = {
                incrementSeconds = it.takeIf { seconds -> seconds > 0 }
                showIncrementDialog = false
            }
        )
    }
}
