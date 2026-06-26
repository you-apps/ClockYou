package com.bnyro.clock.presentation.screens.timer.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.NumberKeypadOperation
import kotlinx.coroutines.launch

@Composable
fun AlarmNumberKeypad(
    onOperation: (NumberKeypadOperation) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val buttonSpacing = 12.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        rows.forEach { rowNumbers ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowNumbers.forEach { number ->
                    AlarmNumPadButton(
                        number = number,
                        modifier = Modifier.weight(1f),
                        onOperation = onOperation
                    )
                }
            }
        }

        // Bottom row
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            AlarmNumPadButton(
                number = "00",
                modifier = Modifier.weight(1f),
                onOperation = onOperation
            )
            AlarmNumPadButton(
                number = "0",
                modifier = Modifier.weight(1f),
                onOperation = onOperation
            )

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
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmNumPadButton(
    number: String,
    modifier: Modifier = Modifier,
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
        modifier = modifier.aspectRatio(1f),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.displaySmall.fontSize
            )
        }
    }
}