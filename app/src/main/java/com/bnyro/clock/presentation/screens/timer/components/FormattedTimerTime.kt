package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.domain.model.TimeUnit

@Composable
fun FormattedTimerTime(
    modifier: Modifier = Modifier,
    seconds: Int
) {
    val remainingSeconds = seconds % 100
    val minutes = seconds / 100 % 100
    val hours = seconds / 10000 % 100

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        FormattedUnitTime(
            unit = TimeUnit.Hours,
            value = hours,
            isActive = hours > 0
        )
        FormattedUnitTime(
            unit = TimeUnit.Minutes,
            value = minutes,
            isActive = minutes > 0 || hours > 0
        )
        FormattedUnitTime(
            unit = TimeUnit.Seconds,
            value = remainingSeconds,
            isActive = seconds > 0 || minutes > 0 || hours > 0
        )
    }
}

@Composable
fun FormattedUnitTime(
    unit: TimeUnit,
    value: Int,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * 1.5f
        )
        Text(
            text = unit.name.first().lowercase(),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.5f
        )
    }
}
