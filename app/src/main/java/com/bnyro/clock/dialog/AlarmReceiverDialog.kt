package com.bnyro.clock.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.components.DisabledTextField
import com.bnyro.clock.ui.model.AlarmModel
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmReceiverDialog(alarm: Alarm) {
    var showDialog by remember {
        mutableStateOf(true)
    }
    val alarmModel: AlarmModel = viewModel()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add_alarm)) },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    DisabledTextField(label = R.string.label, text = alarm.label.orEmpty())
                    val time = TimeHelper.millisToTime(alarm.time)
                    DisabledTextField(
                        label = R.string.time,
                        text = "${time.hours.addZero()}:${time.minutes.addZero()}"
                    )
                    val days = alarm.days.joinToString(", ") { AlarmHelper.availableDays[it] }
                    DisabledTextField(label = R.string.days, text = days)
                    DisabledTextField(
                        label = R.string.vibrate,
                        text = stringResource(
                            if (alarm.vibrate) R.string.yes else R.string.no
                        )
                    )
                    DisabledTextField(
                        label = R.string.enabled,
                        text = stringResource(
                            if (alarm.enabled) R.string.yes else R.string.no
                        )
                    )
                }
            },
            confirmButton = {
                DialogButton(label = android.R.string.ok) {
                    alarmModel.createAlarm(alarm)
                    showDialog = false
                }
            },
            dismissButton = {
                DialogButton(label = android.R.string.cancel) {
                    showDialog = false
                }
            }
        )
    }
}
