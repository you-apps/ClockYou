package com.bnyro.clock.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DisabledTextField(
    @StringRes label: Int,
    text: String
) {
    OutlinedTextField(
        modifier = Modifier.padding(vertical = 5.dp),
        value = text,
        onValueChange = {},
        label = {
            Text(stringResource(label))
        },
        readOnly = true
    )
}
