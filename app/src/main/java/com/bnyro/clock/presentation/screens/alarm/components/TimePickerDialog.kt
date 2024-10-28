package com.bnyro.clock.presentation.screens.alarm.components

import android.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.timer.components.KeyboardPickerState
import com.bnyro.clock.presentation.screens.timer.components.KeyboardTimePicker
import com.bnyro.clock.util.TimeHelper

enum class TimePickerMode {
    CLOCK,
    KEYBOARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    label: String,
    initialMillis: Long? = null,
    onDismissRequest: () -> Unit,
    onChange: (timeInMillis: Int) -> Unit
) {
    val initialTime = initialMillis?.let { TimeHelper.millisToTime(it) }
    val clockPickerState = rememberTimePickerState(
        initialHour = initialTime?.hours ?: 0,
        initialMinute = initialTime?.minutes ?: 0
    )
    val keyboardPickerState by remember {
        mutableStateOf(KeyboardPickerState())
    }

    var currentMode by remember {
        mutableStateOf(TimePickerMode.CLOCK)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableIcon(
                    imageVector = if (currentMode == TimePickerMode.CLOCK) Icons.Default.Keyboard
                    else Icons.Default.AccessTime
                ) {
                    currentMode =
                        if (currentMode == TimePickerMode.CLOCK) TimePickerMode.KEYBOARD else TimePickerMode.CLOCK
                }

                Spacer(modifier = Modifier.weight(1f))

                DialogButton(label = R.string.cancel) {
                    onDismissRequest.invoke()
                }

                DialogButton(label = R.string.ok) {
                    val timeInMillis = when (currentMode) {
                        TimePickerMode.CLOCK -> (clockPickerState.hour * 60 + clockPickerState.minute) * 60 * 1000
                        TimePickerMode.KEYBOARD -> keyboardPickerState.millis
                    }
                    onChange.invoke(timeInMillis)
                }
            }
        },
        title = {
            Text(label)
        },
        text = {
            Spacer(modifier = Modifier.height(10.dp))

            if (currentMode == TimePickerMode.CLOCK) {
                TimePicker(state = clockPickerState)
            } else {
                KeyboardTimePicker(state = keyboardPickerState)
            }
        }
    )
}
