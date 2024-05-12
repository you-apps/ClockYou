package com.bnyro.clock.presentation.screens.clock.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DigitalClockDisplay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val dateTime by produceState(initialValue = TimeHelper.formatDateTime(TimeHelper.getTimeByZone()),
            producer = {
                while (isActive) {
                    value = TimeHelper.formatDateTime(TimeHelper.getTimeByZone())
                    delay(1000)
                }
            })
        Text(
            text = dateTime.second, style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = dateTime.first, style = MaterialTheme.typography.bodyLarge
        )
    }
}