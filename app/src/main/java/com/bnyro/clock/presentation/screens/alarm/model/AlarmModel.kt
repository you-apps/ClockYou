package com.bnyro.clock.presentation.screens.alarm.model

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.R
import com.bnyro.clock.data.database.DatabaseHolder
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.time.Duration.Companion.milliseconds

class AlarmModel(application: Application) : AndroidViewModel(application) {
    private val createUpdateDeleteAlarmUseCase =
        CreateUpdateDeleteAlarmUseCase(application.applicationContext)
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

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.updateAlarm(alarm)
        }
    }

    fun createToast(alarm: Alarm, context: Context) {
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

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.deleteAlarm(alarm)
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