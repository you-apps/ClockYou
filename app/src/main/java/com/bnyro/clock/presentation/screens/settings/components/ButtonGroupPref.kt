package com.bnyro.clock.presentation.screens.settings.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ButtonGroupPref(
    title: String,
    options: List<String>,
    values: List<T>,
    currentValue: T,
    onChange: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        val scrollState = rememberScrollState()
        MultiChoiceSegmentedButtonRow(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            values.forEachIndexed { index, value ->
                val isChecked = if (currentValue is List<*>) {
                    currentValue.contains(value)
                } else {
                    currentValue == value
                }
                SegmentedButton(
                    checked = isChecked,
                    onCheckedChange = { onChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = values.size),
                    modifier = Modifier.widthIn(min = 80.dp)
                ) {
                    Text(
                        text = options[index],
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}