package com.bnyro.clock.presentation.screens.settings.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            .padding(top = 8.dp)
    ) {
        Text(title)
        Spacer(modifier = Modifier.height(8.dp))

        val scrollState = rememberScrollState()
        MultiChoiceSegmentedButtonRow(
            modifier = Modifier
                .horizontalScroll(scrollState)
        ) {
            values.forEachIndexed { index, value ->
                SegmentedButton(
                    checked = currentValue == value,
                    onCheckedChange = { if (it) onChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = values.size)
                ) {
                    Text(text = options[index])
                }
            }
        }
    }
}
