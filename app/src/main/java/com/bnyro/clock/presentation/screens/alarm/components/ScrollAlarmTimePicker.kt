package com.bnyro.clock.presentation.screens.alarm.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.components.ScrollWheel

@Composable
fun ScrollAlarmTimePicker(
    initialHours: Int,
    initialMinutes: Int,
    onHoursChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember { DateFormat.is24HourFormat(context) }

    // Track AM/PM state dynamically based on incoming hours
    val meridiem = if (initialHours >= 12) Meridiem.PM else Meridiem.AM

    // the wheels sit on a page that scrolls the same way they do, so a drag that
    // lands on them is theirs alone and never reaches the page behind
    val keepDragsOnTheWheels = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ) = available.copy(x = 0f)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity) =
                available.copy(x = 0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(keepDragsOnTheWheels),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row {
                ScrollWheel(
                    value = if (is24Hour) {
                        initialHours
                    } else {
                        val h = initialHours % 12
                        if (h == 0) 12 else h
                    },
                    onValueChanged = { selectedHour ->
                        val updatedHours = if (is24Hour) {
                            selectedHour
                        } else {
                            when (meridiem) {
                                Meridiem.AM -> if (selectedHour == 12) 0 else selectedHour
                                Meridiem.PM -> if (selectedHour == 12) 12 else selectedHour + 12
                            }
                        }
                        onHoursChanged(updatedHours)
                    },
                    maxValue = if (is24Hour) 24 else 12,
                    offset = if (is24Hour) 0 else 1
                )

                Spacer(modifier = Modifier.width(16.dp))

                ScrollWheel(
                    value = initialMinutes,
                    onValueChanged = { onMinutesChanged(it) },
                    maxValue = 60
                )

                if (!is24Hour) {
                    Spacer(modifier = Modifier.width(16.dp))
                    ScrollWheel(
                        value = meridiem.ordinal,
                        onValueChanged = { ordinal ->
                            val h = initialHours % 12
                            val current12Hour = if (h == 0) 12 else h

                            val updatedHours = when (Meridiem.entries[ordinal]) {
                                Meridiem.PM -> if (current12Hour == 12) 12 else current12Hour + 12
                                Meridiem.AM -> if (current12Hour == 12) 0 else current12Hour
                            }
                            onHoursChanged(updatedHours)
                        },
                        maxValue = Meridiem.entries.size,
                        label = { Meridiem.entries[it].name }
                    )
                }
            }
        }
    }
}

enum class Meridiem {
    AM, PM
}
