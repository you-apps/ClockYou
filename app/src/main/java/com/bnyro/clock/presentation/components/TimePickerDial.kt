package com.bnyro.clock.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.timer.components.ScrollTimePicker
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun TimePickerDial(timerModel: TimerModel) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.hours),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                ScrollTimePicker(
                    value = remember { timerModel.hours },
                    onValueChanged = { timerModel.hours = it },
                    maxValue = 24
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.minutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                ScrollTimePicker(
                    value = remember { timerModel.minutes },
                    onValueChanged = { timerModel.minutes = it },
                    maxValue = 60
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.seconds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                ScrollTimePicker(
                    value = remember { timerModel.seconds },
                    onValueChanged = { timerModel.seconds = it },
                    maxValue = 60
                )
            }
        }
    }
}