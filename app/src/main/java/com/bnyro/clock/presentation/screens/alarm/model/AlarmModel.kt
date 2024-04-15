package com.bnyro.clock.presentation.screens.alarm.model

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.R
import com.bnyro.clock.data.database.DatabaseHolder
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.time.Duration.Companion.milliseconds

class AlarmModel : ViewModel() {
    var selectedAlarm: Alarm? by mutableStateOf(null)
    var showFilter by mutableStateOf(false)
    val filters = MutableStateFlow(AlarmFilters())
    val alarms: StateFlow<List<Alarm>> =
        combine(DatabaseHolder.instance.alarmsDao().getAllStream(), filters) { items, filter ->
            items.filter { alarm ->
                (filter.startTime <= alarm.time && alarm.time <= filter.endTime)
                        && !Collections.disjoint(filter.weekDays, alarm.days)
                        && (alarm.label?.lowercase()?.contains(filter.label.lowercase())
                    ?: true) && (alarm.formattedTime.lowercase()
                    .contains(filter.label.lowercase()))

            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = listOf()
        )

    fun createAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().insert(alarm)
        }
    }

    fun updateAlarm(context: Context, alarm: Alarm) {
        if (alarm.enabled) {
            val millisRemainingForAlarm =
                (AlarmHelper.getAlarmTime(alarm) - System.currentTimeMillis())
            val formattedDuration =
                TimeHelper.durationToFormatted(context, millisRemainingForAlarm.milliseconds)
            Toast.makeText(
                context,
                context.resources.getString(R.string.alarm_will_play, formattedDuration),
                Toast.LENGTH_SHORT
            ).show()
        }
        AlarmHelper.enqueue(context, alarm)
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().update(alarm)
        }
    }

    fun deleteAlarm(context: Context, alarm: Alarm) {
        AlarmHelper.cancel(context, alarm)
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().delete(alarm)
        }
    }

    fun updateLabelFilter(label: String) {
        filters.update { it.copy(label = label) }
    }

    fun updateWeekDayFilter(weekDays: List<Int>) {
        filters.update { it.copy(weekDays = weekDays) }
    }

    fun updateStartTimeFilter(startTime: Long) {
        filters.update { it.copy(startTime = startTime) }
    }

    fun updateEndTimeFilter(endTime: Long) {
        filters.update { it.copy(endTime = endTime) }
    }

    fun resetFilters() {
        filters.update { AlarmFilters() }
    }
}