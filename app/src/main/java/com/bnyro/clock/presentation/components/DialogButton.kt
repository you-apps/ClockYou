package com.bnyro.clock.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DialogButton(
    @StringRes label: Int,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(text = stringResource(id = label))
    }
}
