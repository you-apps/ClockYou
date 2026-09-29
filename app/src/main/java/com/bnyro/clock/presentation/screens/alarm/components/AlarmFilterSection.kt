package com.bnyro.clock.presentation.screens.alarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.BorderColor
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.ui.theme.LabelColor
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper

@Composable
fun AlarmFilterSection(
    filters: AlarmFilters,
    labelColors: List<Int>,
    onChangeLabel: (String) -> Unit,
    onChangeLabelColors: (Set<Int>) -> Unit,
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

        if (labelColors.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.BorderColor, contentDescription = stringResource(R.string.label_color))
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    labelColors.forEach { color ->
                        val selected = color in filters.labelColors
                        val preset = LabelColor.entries.firstOrNull { it.argb == color }
                        val name = preset?.let { stringResource(it.label) }
                            ?: "${stringResource(R.string.custom_color)} ${String.format("#%06X", color and 0xFFFFFF)}"
                        Box(
                            Modifier
                                .size(30.dp)
                                .then(
                                    if (selected) Modifier.background(Color(color), CircleShape)
                                    else Modifier.border(4.dp, Color(color), CircleShape)
                                )
                                .clip(CircleShape)
                                .semantics { contentDescription = name }
                                .toggleable(value = selected, role = Role.Checkbox) {
                                    onChangeLabelColors(
                                        if (selected) filters.labelColors - color
                                        else filters.labelColors + color
                                    )
                                }
                        )
                    }
                }
            }
        }

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
        val daysOfWeek = remember { AlarmHelper.getDaysOfWeekForDisplay(context) }
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
    val context = LocalContext.current

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
            Text(text = TimeHelper.millisToFormatted(context, startTime))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )

        Button(onClick = onClickEndTime, modifier = Modifier.weight(1f)) {
            Text(text = TimeHelper.millisToFormatted(context, endTime))
        }

    }
}
