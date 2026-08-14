package com.bnyro.clock.presentation.screens.alarmpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.presentation.screens.alarmpicker.components.AlarmPicker
import com.bnyro.clock.presentation.screens.alarmpicker.model.AlarmPickerModel
import com.bnyro.clock.util.AlarmHelper

@Composable
fun AlarmPickerScreen(onNavigateBack: () -> Unit) {
    val viewModel: AlarmPickerModel = viewModel()
    val context = LocalContext.current
    AlarmPicker(
        onCancel = { onNavigateBack.invoke() },
        currentAlarm = viewModel.alarm,
        onDelete = { alarm ->
            viewModel.deleteAlarm(alarm)
            onNavigateBack.invoke()
        },
        onSave = { alarm ->
            if (alarm.id == 0L) {
                // Create New Alarm and enable by default
                viewModel.createAlarm(alarm.copy(enabled = true))
                AlarmHelper.showAlarmScheduledToast(context, alarm)
            } else {
                // Update Alarm and enable automatically
                viewModel.updateAlarm(alarm.copy(enabled = true))
                AlarmHelper.showAlarmScheduledToast(context, alarm)
            }
            onNavigateBack.invoke()
        })
}
