package com.bnyro.clock.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockTimePicker(
    initialHours: Int,
    initialMinutes: Int,
    is24Hour: Boolean,
    onHoursChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHours,
        initialMinute = initialMinutes,
        is24Hour = is24Hour
    )

    LaunchedEffect(state.hour, state.minute) {
        onHoursChanged(state.hour)
        onMinutesChanged(state.minute)
    }

    TimePicker(state = state)
}
