package com.bnyro.clock.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val BUTTONS_SIZE = 90.dp

@Composable
fun NumberKeypad(
    onOperation: (Operation) -> Unit,
) {
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
    SingleElementButton(
        onClick = {
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
