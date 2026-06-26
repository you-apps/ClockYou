package com.bnyro.clock.presentation.screens.alarm.components

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.screens.timer.components.ScrollTimePicker

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

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row {
                ScrollTimePicker(
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

                ScrollTimePicker(
                    value = initialMinutes,
                    onValueChanged = { onMinutesChanged(it) },
                    maxValue = 60
                )

                if (!is24Hour) {
                    Spacer(modifier = Modifier.width(16.dp))
                    MeridiemPicker(value = meridiem, onValueChanged = { newMeridiem ->
                        val h = initialHours % 12
                        val current12Hour = if (h == 0) 12 else h

                        val updatedHours = when (newMeridiem) {
                            Meridiem.PM -> if (current12Hour == 12) 12 else current12Hour + 12
                            Meridiem.AM -> if (current12Hour == 12) 0 else current12Hour
                        }
                        onHoursChanged(updatedHours)
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeridiemPicker(
    value: Meridiem,
    onValueChanged: (Meridiem) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val state = rememberPagerState(initialPage = 200 + value.ordinal + 1) {
        400
    }
    val currentPage = state.currentPage + 1

    LaunchedEffect(currentPage) {
        onValueChanged(Meridiem.entries[currentPage % 2])
    }

    VerticalPager(
        modifier = Modifier.height(224.dp),
        state = state,
        pageSpacing = 16.dp,
        pageSize = PageSize.Fixed(64.dp)
    ) { index ->
        Text(
            text = Meridiem.entries[index % 2].name,
            style = MaterialTheme.typography.displayMedium,
            color = if (index == currentPage) primary else primaryMuted
        )
    }
}

enum class Meridiem {
    AM, PM
}