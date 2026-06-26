package com.bnyro.clock.presentation.screens.alarm.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.domain.model.NumberKeypadOperation
import com.bnyro.clock.presentation.screens.timer.components.AlarmNumberKeypad
import com.bnyro.clock.presentation.screens.timer.components.NumberKeypad

@Composable
fun AlarmTimePicker(
    initialHours: Int,
    initialMinutes: Int,
    onHoursChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember { DateFormat.is24HourFormat(context) }
    var meridiem by remember { mutableStateOf(if (initialHours >= 12) Meridiem.PM else Meridiem.AM) }
    var inputDigits by remember { mutableStateOf("") }

    val pushTimeUpdate = { digits: String ->
        val truncated = if (digits.length > 4) digits.takeLast(4) else digits
        inputDigits = truncated

        val padded = truncated.padStart(4, '0')
        var rawHours = padded.substring(0, 2).toIntOrNull() ?: 0
        val minutesInt = padded.substring(2, 4).toIntOrNull() ?: 0

        if (is24Hour) {
            rawHours = rawHours.coerceIn(0, 23)
        } else {
            val current12Hour = rawHours.coerceIn(1, 12)
            rawHours = when (meridiem) {
                Meridiem.AM -> if (current12Hour == 12) 0 else current12Hour
                Meridiem.PM -> if (current12Hour == 12) 12 else current12Hour + 12
            }
        }

        onHoursChanged(rawHours)
        onMinutesChanged(minutesInt.coerceIn(0, 59))
    }

    LaunchedEffect(meridiem) {
        pushTimeUpdate(inputDigits)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            val displayString = inputDigits.padStart(4, '0')

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayString.substring(0, 2),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp, fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayString.substring(2, 4),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!is24Hour) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AmPmSelector(
                            label = Meridiem.AM.name, isSelected = meridiem == Meridiem.AM
                        ) {
                            meridiem = Meridiem.AM
                        }
                        AmPmSelector(
                            label = Meridiem.PM.name, isSelected = meridiem == Meridiem.PM
                        ) {
                            meridiem = Meridiem.PM
                        }
                    }
                }
            }
        }

        AlarmNumberKeypad(
            onOperation = { operation ->
                when (operation) {
                    is NumberKeypadOperation.AddNumber -> {
                        val currentText = if (inputDigits == "0000") "" else inputDigits
                        pushTimeUpdate(currentText + operation.number)
                    }

                    is NumberKeypadOperation.Delete -> {
                        if (inputDigits.isNotEmpty()) {
                            pushTimeUpdate(inputDigits.dropLast(1))
                        }
                    }

                    is NumberKeypadOperation.Clear -> {
                        pushTimeUpdate("")
                    }
                }
            })
    }
}

@Composable
private fun AmPmSelector(
    label: String, isSelected: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}