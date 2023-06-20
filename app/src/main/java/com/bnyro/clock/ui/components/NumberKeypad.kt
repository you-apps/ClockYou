package com.bnyro.clock.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

val BUTTONS_SIZE = 90.dp

@Composable
fun NumberKeypad(
    onOperation: (Operation) -> Unit,
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val buttonSpacing = 6.dp

    Column(
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            Button(number = "1", onOperation = onOperation)
            Button(number = "2", onOperation = onOperation)
            Button(number = "3", onOperation = onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        ) {
            Button(number = "4", onOperation = onOperation)
            Button(number = "5", onOperation = onOperation)
            Button(number = "6", onOperation = onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        ) {
            Button(number = "7", onOperation = onOperation)
            Button(number = "8", onOperation = onOperation)
            Button(number = "9", onOperation = onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
        ) {
            Button(number = "00", onOperation = onOperation)
            Button(number = "0", onOperation = onOperation)
            SingleElementButton(
                onClick = {
                    coroutineScope.launch {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }

                    onOperation(Operation.Delete)
                },
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(BUTTONS_SIZE),
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint =  MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun Button(
    number: String,
    onOperation: (Operation) -> Unit,
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    SingleElementButton(
        onClick = {
            coroutineScope.launch {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }

            onOperation(Operation.AddNumber(number))
        },
        modifier = Modifier.size(BUTTONS_SIZE),
    ) {
        Text(
            text = number,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize
        )
    }
}

sealed class Operation {
    class AddNumber(val number: String) : Operation()
    object Delete: Operation()
}
