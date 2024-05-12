package com.bnyro.clock.presentation.screens.alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.timer.components.ScrollTimePicker

@Composable
fun SnoozeTimePickerDialog(
    onDismissRequest: () -> Unit,
    currentTime: Int,
    onTimeSet: (Int) -> Unit
) {
    var newTime = remember { currentTime }
    AlertDialog(onDismissRequest, confirmButton = {
        DialogButton(label = android.R.string.ok) {
            onTimeSet(newTime)
        }
    }, title = { Text(text = stringResource(R.string.select_snooze_time)) }, text = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ScrollTimePicker(value = currentTime, onValueChanged = {
                newTime = it
            }, maxValue = 120, offset = 1)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.minutes),
                style = MaterialTheme.typography.displaySmall
            )
        }
    })
}

@Preview
@Composable
private fun SnoozePickerPreview() {
    SnoozeTimePickerDialog(onDismissRequest = { }, currentTime = 10, onTimeSet = {})
}
