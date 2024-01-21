package com.bnyro.clock.ui.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.obj.AlarmFilters
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

class AlarmModel : ViewModel() {
    var selectedAlarm: Alarm? by mutableStateOf(null)
    var showFilter by mutableStateOf(false)
    var filters = MutableStateFlow(AlarmFilters())
        private set
    var alarms: MutableStateFlow<List<Alarm>> = MutableStateFlow(emptyList())
        private set

    init {
        getAlarms()
    }

    private fun getAlarms() {
        viewModelScope.launch {
            alarms.value = DatabaseHolder.instance.alarmsDao().getAll().filter { alarm ->
                (filters.value.startTime <= alarm.time && alarm.time <= filters.value.endTime)
                        && !Collections.disjoint(filters.value.weekDays, alarm.days)
                        && (alarm.label?.lowercase()?.contains(filters.value.label.lowercase()) ?: true)

            }
        }
    }

    fun createAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().insert(alarm)
            getAlarms()
        }
    }

    fun updateAlarm(context: Context, alarm: Alarm) {
        AlarmHelper.enqueue(context, alarm)
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().update(alarm)
            getAlarms()
        }
    }

    fun deleteAlarm(context: Context, alarm: Alarm) {
        AlarmHelper.cancel(context, alarm)
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().delete(alarm)
            getAlarms()
        }
    }

    fun updateLabelFilter(label: String) {
        filters.update { it.copy(label = label) }
        getAlarms()
    }

    fun updateWeekDayFilter(weekDays: List<Int>) {
        filters.update { it.copy(weekDays = weekDays) }
        getAlarms()
    }

    fun updateStartTimeFilter(startTime: Long) {
        filters.update { it.copy(startTime = startTime) }
        getAlarms()
    }

    fun updateEndTimeFilter(endTime: Long) {
        filters.update { it.copy(endTime = endTime) }
        getAlarms()
    }

    fun resetFilters() {
        filters.update { AlarmFilters() }
        getAlarms()
    }
}
