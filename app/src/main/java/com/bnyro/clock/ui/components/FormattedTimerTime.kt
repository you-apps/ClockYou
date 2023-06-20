package com.bnyro.clock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormattedTimerTime(
    modifier: Modifier = Modifier,
    seconds: Int,
) {
    val remainingSeconds = seconds % 100
    val minutes = seconds / 100 % 100
    val hours = seconds / 10000 % 100

    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        FormattedUnitTime(unit = TimeUnit.Hours, value = hours)
        FormattedUnitTime(unit = TimeUnit.Minutes, value = minutes)
        FormattedUnitTime(unit = TimeUnit.Seconds, value = remainingSeconds)
    }
}

enum class TimeUnit {
    Hours,
    Minutes,
    Seconds,
}

@Composable
fun FormattedUnitTime(
    unit: TimeUnit,
    value: Int,
) {
    val isActive = value > 0

    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * 1.5f,
        )
        Text(
            text = unit.name.first().lowercase(),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.5f,
        )
    }
}
