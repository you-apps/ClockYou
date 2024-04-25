package com.bnyro.clock.presentation.features

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarmpicker.components.AlarmPicker
import com.bnyro.clock.presentation.screens.alarmpicker.model.AlarmPickerModel

@Composable
fun AlarmReceiverDialog(context: Context, alarm: Alarm) {
    var showDialog by remember {
        mutableStateOf(true)
    }

    if (showDialog) {
        val alarmModel: AlarmPickerModel = viewModel()
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AlarmPicker(
                onCancel = { showDialog = false },
                currentAlarm = alarm,
                onSave = {
                    alarmModel.createAlarm(alarm)
                    alarmModel.createToast(alarm, context)
                }
            )
        }
    }
}
