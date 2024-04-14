package com.bnyro.clock.presentation.screens.timer.components

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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.NumberKeypadOperation
import kotlinx.coroutines.launch

@Composable
fun NumberKeypad(
    onOperation: (NumberKeypadOperation) -> Unit
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp

    val buttonSize = (screenHeight / 8.5).dp
    val buttonSpacing = 6.dp

    Column(
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.weight(1f)
        ) {
            NumPadButton(number = "1", buttonSize, onOperation)
            NumPadButton(number = "2", buttonSize, onOperation)
            NumPadButton(number = "3", buttonSize, onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.weight(1f)
        ) {
            NumPadButton(number = "4", buttonSize, onOperation)
            NumPadButton(number = "5", buttonSize, onOperation)
            NumPadButton(number = "6", buttonSize, onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.weight(1f)
        ) {
            NumPadButton(number = "7", buttonSize, onOperation)
            NumPadButton(number = "8", buttonSize, onOperation)
            NumPadButton(number = "9", buttonSize, onOperation)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.weight(1f)
        ) {
            NumPadButton(number = "00", buttonSize, onOperation)
            NumPadButton(number = "0", buttonSize, onOperation)
            SingleElementButton(
                onClick = {
                    coroutineScope.launch {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }

                    onOperation(NumberKeypadOperation.Delete)
                },
                onLongClick = {
                    coroutineScope.launch {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }

                    onOperation(NumberKeypadOperation.Clear)
                },
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(buttonSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun NumPadButton(
    number: String,
    buttonSize: Dp,
    onOperation: (NumberKeypadOperation) -> Unit
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    SingleElementButton(
        onClick = {
            coroutineScope.launch {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }

            onOperation(NumberKeypadOperation.AddNumber(number))
        },
        modifier = Modifier.size(buttonSize),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Text(
            text = number,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.displaySmall.fontSize
        )
    }
}
