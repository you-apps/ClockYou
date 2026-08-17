package com.bnyro.clock.presentation.screens.alarmpicker.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.MonthlyRepeat
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.components.DialogButtonStyle
import com.bnyro.clock.util.AlarmHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private const val MILLIS_PER_DAY = 86_400_000L
private val SECTION_INDENT = 56.dp

enum class RecurrenceEnd {
    NEVER,
    ON_DATE,
    AFTER_OCCURRENCES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencePicker(
    startDate: Long,
    repeatInterval: Int,
    repeatUnit: RepeatUnit,
    monthlyRepeat: MonthlyRepeat,
    chosenDays: MutableList<Int>,
    endDate: Long?,
    endOccurrences: Int?,
    onStartDateChange: (Long) -> Unit,
    onRepeatIntervalChange: (Int) -> Unit,
    onRepeatUnitChange: (RepeatUnit) -> Unit,
    onMonthlyRepeatChange: (MonthlyRepeat) -> Unit,
    onEndChange: (endDate: Long?, endOccurrences: Int?) -> Unit
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    val startDateFormatter = remember {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEMMMdy"), locale)
    }
    val endDateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showUnits by remember { mutableStateOf(false) }
    var showMonthlyRepeats by remember { mutableStateOf(false) }

    var intervalInput by remember { mutableStateOf(repeatInterval.toString()) }
    var occurrencesInput by remember { mutableStateOf((endOccurrences ?: 1).toString()) }
    var chosenEndDate by remember {
        mutableLongStateOf(endDate ?: LocalDate.ofEpochDay(startDate).plusMonths(1).toEpochDay())
    }
    var chosenEndOccurrences by remember { mutableIntStateOf(endOccurrences ?: 1) }
    var endMode by remember {
        mutableStateOf(
            when {
                endDate != null -> RecurrenceEnd.ON_DATE
                endOccurrences != null -> RecurrenceEnd.AFTER_OCCURRENCES
                else -> RecurrenceEnd.NEVER
            }
        )
    }

    val startLocalDate = LocalDate.ofEpochDay(startDate)

    Surface(modifier = Modifier.clickable { showStartDatePicker = true }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Event,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = stringResource(R.string.start_date),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (startLocalDate == LocalDate.now()) {
                        stringResource(R.string.today)
                    } else {
                        startDateFormatter.format(startLocalDate)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.EventRepeat,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = stringResource(R.string.repeats_every),
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.width(88.dp),
                    value = intervalInput,
                    onValueChange = { input ->
                        intervalInput = input.filter(Char::isDigit).take(3)
                        onRepeatIntervalChange(intervalInput.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(1f),
                    expanded = showUnits,
                    onExpandedChange = { showUnits = !showUnits }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        value = pluralStringResource(repeatUnit.value, repeatInterval),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showUnits) }
                    )
                    ExposedDropdownMenu(
                        expanded = showUnits,
                        onDismissRequest = { showUnits = false }
                    ) {
                        RepeatUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = pluralStringResource(unit.value, repeatInterval))
                                },
                                onClick = {
                                    onRepeatUnitChange(unit)
                                    showUnits = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = repeatUnit == RepeatUnit.WEEK || repeatUnit == RepeatUnit.MONTH
    ) {
        Column(modifier = Modifier.padding(start = SECTION_INDENT, end = 8.dp, bottom = 8.dp)) {
            Text(
                text = stringResource(R.string.repeats_on),
                style = MaterialTheme.typography.titleLarge
            )
            if (repeatUnit == RepeatUnit.WEEK) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = remember {
                        AlarmHelper.getDaysOfWeekByLocale(context)
                    }

                    daysOfWeek.forEach { (day, index) ->
                        val enabled = chosenDays.contains(index)
                        Box(
                            modifier = Modifier
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
            } else {
                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(top = 8.dp),
                    expanded = showMonthlyRepeats,
                    onExpandedChange = { showMonthlyRepeats = !showMonthlyRepeats }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        value = monthlyRepeatLabel(startLocalDate, monthlyRepeat),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(showMonthlyRepeats)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showMonthlyRepeats,
                        onDismissRequest = { showMonthlyRepeats = false }
                    ) {
                        MonthlyRepeat.entries.forEach { repeat ->
                            DropdownMenuItem(
                                text = { Text(text = monthlyRepeatLabel(startLocalDate, repeat)) },
                                onClick = {
                                    onMonthlyRepeatChange(repeat)
                                    showMonthlyRepeats = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.padding(start = SECTION_INDENT, end = 8.dp, bottom = 8.dp)) {
        Text(text = stringResource(R.string.ends), style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = endMode == RecurrenceEnd.NEVER,
                    onClick = {
                        endMode = RecurrenceEnd.NEVER
                        onEndChange(null, null)
                    }
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = endMode == RecurrenceEnd.NEVER, onClick = null)
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.ends_never)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = endMode == RecurrenceEnd.ON_DATE,
                    onClick = {
                        endMode = RecurrenceEnd.ON_DATE
                        onEndChange(chosenEndDate, null)
                    }
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = endMode == RecurrenceEnd.ON_DATE, onClick = null)
            Text(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                text = stringResource(R.string.ends_on)
            )
            OutlinedButton(onClick = { showEndDatePicker = true }) {
                Text(text = endDateFormatter.format(LocalDate.ofEpochDay(chosenEndDate)))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = endMode == RecurrenceEnd.AFTER_OCCURRENCES,
                    onClick = {
                        endMode = RecurrenceEnd.AFTER_OCCURRENCES
                        onEndChange(null, chosenEndOccurrences)
                    }
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = endMode == RecurrenceEnd.AFTER_OCCURRENCES, onClick = null)
            Text(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                text = stringResource(R.string.ends_after)
            )
            OutlinedTextField(
                modifier = Modifier.width(88.dp),
                value = occurrencesInput,
                onValueChange = { input ->
                    occurrencesInput = input.filter(Char::isDigit).take(3)
                    chosenEndOccurrences = occurrencesInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    endMode = RecurrenceEnd.AFTER_OCCURRENCES
                    onEndChange(null, chosenEndOccurrences)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                text = pluralStringResource(R.plurals.occurrences, chosenEndOccurrences)
            )
        }
    }

    if (showStartDatePicker) {
        RecurrenceDatePickerDialog(
            selectedDate = startDate,
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { date ->
                onStartDateChange(date)
                showStartDatePicker = false
            }
        )
    }
    if (showEndDatePicker) {
        RecurrenceDatePickerDialog(
            selectedDate = chosenEndDate,
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { date ->
                chosenEndDate = date
                endMode = RecurrenceEnd.ON_DATE
                onEndChange(date, null)
                showEndDatePicker = false
            }
        )
    }
}

/**
 * @return how a monthly repetition anchored at [date] is described, for example
 * "Monthly on day 17" or "Monthly on the third Monday".
 */
@Composable
private fun monthlyRepeatLabel(date: LocalDate, monthlyRepeat: MonthlyRepeat): String {
    return when (monthlyRepeat) {
        MonthlyRepeat.DAY_OF_MONTH -> stringResource(R.string.monthly_on_day, date.dayOfMonth)
        MonthlyRepeat.DAY_OF_WEEK -> stringResource(
            R.string.monthly_on_weekday,
            stringArrayResource(R.array.month_week_ordinals)[(date.dayOfMonth - 1) / 7],
            date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceDatePickerDialog(
    selectedDate: Long,
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate * MILLIS_PER_DAY
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(label = R.string.save, style = DialogButtonStyle.PRIMARY) {
                datePickerState.selectedDateMillis?.let { onDateSelected(it / MILLIS_PER_DAY) }
            }
        },
        dismissButton = {
            DialogButton(
                label = android.R.string.cancel,
                style = DialogButtonStyle.SECONDARY
            ) {
                onDismissRequest()
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
