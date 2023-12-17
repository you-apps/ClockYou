package com.bnyro.clock.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.components.AlarmSettingsSheet
import com.bnyro.clock.ui.model.AlarmModel

@Composable
fun AlarmReceiverDialog(alarm: Alarm) {
    var showSheet by remember {
        mutableStateOf(true)
    }
    val alarmModel: AlarmModel = viewModel()

    if (showSheet) {
        AlarmSettingsSheet(
            onDismissRequest = { showSheet = false },
            currentAlarm = alarm,
            onSave = {
                alarmModel.createAlarm(alarm)
            }
        )
    }
}
