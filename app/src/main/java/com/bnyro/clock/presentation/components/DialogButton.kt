package com.bnyro.clock.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

enum class DialogButtonStyle {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE
}

@Composable
fun DialogButton(
    @StringRes label: Int,
    style: DialogButtonStyle,
    onClick: () -> Unit
) {
    when (style) {
        DialogButtonStyle.PRIMARY -> Button(onClick = onClick) {
            Text(text = stringResource(id = label))
        }
        DialogButtonStyle.SECONDARY -> OutlinedButton(onClick = onClick) {
            Text(text = stringResource(id = label))
        }
        DialogButtonStyle.DESTRUCTIVE -> FilledTonalButton(
            onClick = onClick,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = stringResource(id = label))
        }
    }
}
