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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.screens.timer.components.ScrollTimePicker

/**
 * @param initialHours Initial Hours according to 24 hour format 0-23
 * @param initialMinutes Initial Minutes
 * @param onHoursChanged New hour value in 24 hour format 0-23
 * @param onMinutesChanged New minutes value
 */
@Composable
fun AlarmTimePicker(
    initialHours: Int,
    initialMinutes: Int,
    onHoursChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember { DateFormat.is24HourFormat(context) }
    val meridiem = remember {
        if (initialHours >= 12) Meridiem.PM else Meridiem.AM
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row {
                /**
                 * Hour value in 24 hour format 0-23
                 */
                /**
                 * Hour value in 24 hour format 0-23
                 */
                var hours = remember { initialHours }
                ScrollTimePicker(
                    value = if (is24Hour) initialHours else initialHours % 24,
                    onValueChanged = {
                        hours = if (is24Hour) {
                            it
                        } else {
                            when (meridiem) {
                                Meridiem.AM -> {
                                    if (it == 12) 0 else it
                                }

                                Meridiem.PM -> {
                                    if (it == 12) 12 else it + 12
                                }
                            }
                        }
                        assert(hours < 24)
                        onHoursChanged(
                            hours
                        )
                    },
                    maxValue = if (is24Hour) 24 else 12,
                    offset = if (is24Hour) 0 else 1
                )
                Spacer(modifier = Modifier.width(16.dp))
                ScrollTimePicker(value = initialMinutes, onValueChanged = {
                    onMinutesChanged(it)
                }, maxValue = 60)
                if (!is24Hour) {
                    Spacer(modifier = Modifier.width(16.dp))
                    MeridiemPicker(value = meridiem, onValueChanged = {
                        hours = when (it) {
                            Meridiem.PM -> {
                                if (hours >= 12) {
                                    hours
                                } else {
                                    hours + 12
                                }
                            }

                            Meridiem.AM -> if (hours >= 12) {
                                hours - 12
                            } else {
                                hours
                            }
                        }
                        assert(hours < 24)
                        onHoursChanged(
                            hours
                        )
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

@Preview
@Composable
fun AlarmTimePickerPreview() {
    AlarmTimePicker(10, 20, {}) { }
}