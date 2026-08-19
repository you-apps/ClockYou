package com.bnyro.clock.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R

@Composable
fun <T> RadioPickerDialog(
    onDismissRequest: () -> Unit,
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    description: @Composable (T) -> String? = { null },
    onOptionSelected: (T) -> Unit
) {
    var newOption by remember { mutableStateOf(selected) }
    AlertDialog(onDismissRequest, confirmButton = {
        DialogButton(label = R.string.save, style = DialogButtonStyle.PRIMARY) {
            onOptionSelected(newOption)
        }
    }, dismissButton = {
        DialogButton(label = android.R.string.cancel, style = DialogButtonStyle.SECONDARY) {
            onDismissRequest()
        }
    }, title = { Text(text = title) }, text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { newOption = option }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == newOption,
                        onClick = null
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.titleMedium
                        )
                        description(option)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    })
}
