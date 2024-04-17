package com.bnyro.clock.presentation.features

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarm.components.AlarmSettingsSheet
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel

@Composable
fun AlarmReceiverDialog(context: Context, alarm: Alarm) {
    var showSheet by remember {
        mutableStateOf(true)
    }
    val alarmModel: AlarmModel = viewModel()

    if (showSheet) {
        AlarmSettingsSheet(
            onDismissRequest = { showSheet = false },
            currentAlarm = alarm,
            onSave = {
                alarmModel.createAlarm(context, alarm)
            }
        )
    }
}
