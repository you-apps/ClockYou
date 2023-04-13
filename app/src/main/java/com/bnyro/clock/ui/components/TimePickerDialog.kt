package com.bnyro.clock.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.util.TimeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    label: String,
    initialMillis: Long? = null,
    onDismissRequest: () -> Unit,
    onChange: (timeInMillis: Int) -> Unit
) {
    val initialTime = initialMillis?.let { TimeHelper.millisToTime(it) }
    val state = rememberTimePickerState(
        initialHour = initialTime?.hours ?: 0,
        initialMinute = initialTime?.minutes ?: 0
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(label = android.R.string.ok) {
                val timeInMillis = (state.hour * 60 + state.minute) * 60 * 1000
                onChange.invoke(timeInMillis)
            }
        },
        dismissButton = {
            DialogButton(label = android.R.string.cancel) {
                onDismissRequest.invoke()
            }
        },
        title = {
            Text(label)
        },
        text = {
            Spacer(modifier = Modifier.height(10.dp))
            TimePicker(state = state)
        }
    )
}
