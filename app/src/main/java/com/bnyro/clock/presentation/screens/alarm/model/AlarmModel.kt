package com.bnyro.clock.presentation.screens.alarm.model

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.App
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.domain.repository.AlarmRepository
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

class AlarmModel(application: Application) : AndroidViewModel(application) {
    private val alarmRepository: AlarmRepository = (application as App).container.alarmRepository
    private val createUpdateDeleteAlarmUseCase =
        CreateUpdateDeleteAlarmUseCase(application.applicationContext, alarmRepository)

    var showFilter by mutableStateOf(false)
    var showSortOrder by mutableStateOf(false)
    val filters = MutableStateFlow(AlarmFilters())
    private val selectedSortOrder = MutableStateFlow(
        AlarmSortOrder.entries.firstOrNull {
            it.name == Preferences.instance.getString(Preferences.alarmSortOrderKey, null)
        } ?: AlarmSortOrder.UPCOMING
    )
    val sortOrder = selectedSortOrder.asStateFlow()
    private val allAlarms = alarmRepository.getAlarmsStream().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )
    val labelColors = allAlarms.map { items -> items.map { it.labelColor }.distinct() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )

    private val currentMinute = flow {
        while (true) {
            emit(System.currentTimeMillis() / 60_000L)
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }

    val alarms: StateFlow<List<Alarm>> =
        combine(
            allAlarms,
            filters,
            sortOrder,
            currentMinute
        ) { items, filter, sortOrder, _ ->
            val filtered = items.filter { alarm ->
                (filter.startTime <= alarm.time && alarm.time <= filter.endTime)
                        && (filter.labelColors.isEmpty() || alarm.labelColor in filter.labelColors)
                        && !Collections.disjoint(filter.weekDays, alarm.days)
                        && (alarm.label.orEmpty().contains(filter.label, ignoreCase = true)
                        || TimeHelper.millisToFormatted(getApplication(), alarm.time)
                            .contains(filter.label, ignoreCase = true))

            }

            sortOrder.sort(filtered)
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

    fun dismissUpcomingAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.dismissUpcomingAlarm(alarm.copy())
        }
    }

    fun copyAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.createAlarm(alarm.copy(id = 0L))
        }
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

    fun setSortOrder(order: AlarmSortOrder) {
        Preferences.edit { putString(Preferences.alarmSortOrderKey, order.name) }
        selectedSortOrder.update { order }
    }

    fun resetFilters() {
        filters.update { AlarmFilters() }
    }
}
