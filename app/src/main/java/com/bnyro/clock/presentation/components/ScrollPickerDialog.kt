package com.bnyro.clock.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R

@Composable
fun ScrollPickerDialog(
    onDismissRequest: () -> Unit,
    title: String,
    unit: String,
    value: Int,
    maxValue: Int,
    offset: Int = 0,
    label: (Int) -> String = { String.format("%02d", it) },
    onValueSet: (Int) -> Unit
) {
    var newValue = remember { value }
    AlertDialog(onDismissRequest, confirmButton = {
        DialogButton(label = R.string.save, style = DialogButtonStyle.PRIMARY) {
            onValueSet(newValue)
        }
    }, dismissButton = {
        DialogButton(label = android.R.string.cancel, style = DialogButtonStyle.SECONDARY) {
            onDismissRequest()
        }
    }, title = { Text(text = title) }, text = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ScrollWheel(
                value = value,
                onValueChanged = { newValue = it },
                maxValue = maxValue,
                offset = offset,
                label = label
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.offset(y = (-8).dp)
            )
        }
    })
}

@Preview
@Composable
private fun ScrollPickerDialogPreview() {
    ScrollPickerDialog(
        onDismissRequest = { },
        title = "Select snooze length",
        unit = "minutes",
        value = 10,
        maxValue = 120,
        offset = 1,
        label = { it.toString() },
        onValueSet = {}
    )
}
