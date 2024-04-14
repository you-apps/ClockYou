package com.bnyro.clock.presentation.screens.alarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmFilterSection(
    filters: AlarmFilters,
    onChangeLabel: (String) -> Unit,
    onClickWeekDay: (List<Int>) -> Unit,
    onClickStartTime: (Long) -> Unit,
    onClickEndTime: (Long) -> Unit
) {

    var timeFromFilter by remember { mutableStateOf(false) }
    var timeToFilter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = filters.label,
            label = { Text(text = stringResource(id = R.string.alarm_name)) },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
            },
            singleLine = true,
            shape = CircleShape,
            onValueChange = { onChangeLabel(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        )

        WeekDayRow(weekDays = filters.weekDays, onClickWeekDay = onClickWeekDay)

        TimeRangeRow(
            startTime = filters.startTime,
            endTime = filters.endTime,
            onClickStartTime = { timeFromFilter = !timeFromFilter },
            onClickEndTime = { timeToFilter = !timeToFilter }
        )

        if (timeFromFilter) {
            TimePickerDialog(
                label = stringResource(R.string.from),
                onDismissRequest = { timeFromFilter = false }
            ) {
                onClickStartTime(it.toLong())
                timeFromFilter = false
            }
        }

        if (timeToFilter) {
            TimePickerDialog(
                label = stringResource(R.string.to),
                onDismissRequest = { timeToFilter = false }
            ) {
                onClickEndTime(it.toLong())
                timeToFilter = false
            }
        }
    }


}

@Composable
fun WeekDayRow(weekDays: List<Int>, onClickWeekDay: (List<Int>) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val daysOfWeek = remember { AlarmHelper.getDaysOfWeekByLocale(context) }
        val chosenDays = remember { weekDays.toMutableList() }

        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        daysOfWeek.forEach { (day, index) ->
            val enabled = chosenDays.contains(index)
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(30.dp)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    )
                    .clip(CircleShape)
                    .border(
                        if (enabled) 0.dp else 1.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .clickable {
                        if (enabled) {
                            if (chosenDays.size > 1) chosenDays.remove(index)
                        } else {
                            chosenDays.add(
                                index
                            )
                        }
                        onClickWeekDay(chosenDays.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

        }
    }
}

@Composable
fun TimeRangeRow(
    startTime: Long,
    endTime: Long,
    onClickStartTime: () -> Unit,
    onClickEndTime: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccessTimeFilled,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Button(onClick = onClickStartTime, modifier = Modifier.weight(1f)) {
            Text(text = TimeHelper.millisToFormatted(startTime))
        }

        Icon(
            imageVector = Icons.Default.ArrowRightAlt,
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )

        Button(onClick = onClickEndTime, modifier = Modifier.weight(1f)) {
            Text(text = TimeHelper.millisToFormatted(endTime))
        }

    }
}
