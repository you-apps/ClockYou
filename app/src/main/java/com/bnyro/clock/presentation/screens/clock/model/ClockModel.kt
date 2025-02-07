package com.bnyro.clock.presentation.screens.clock.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.App
import com.bnyro.clock.domain.model.SortOrder
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClockModel(application: Application) : AndroidViewModel(application) {
    private val timezoneRepository = (application as App).container.timezoneRepository

    private val sortOrderPref =
        Preferences.instance.getString(Preferences.clockSortOrder, "").orEmpty()
    val sortOrder =
        MutableStateFlow(if (sortOrderPref.isNotEmpty()) SortOrder.valueOf(sortOrderPref) else SortOrder.ALPHABETIC)
    val timeZones = timezoneRepository.getTimezonesForCountries(application.applicationContext)
    var selectedTimeZones = combine(
        timezoneRepository.getTimezonesStream(),
        sortOrder
    ) { selectedZones, sortOrder ->
        val zones = selectedZones.distinct()
        when (sortOrder) {
            SortOrder.ALPHABETIC -> zones.sortedBy { it.zoneName }
            SortOrder.OFFSET -> zones.sortedBy { TimeHelper.getOffsetMillisByZoneId(it.zoneId) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = listOf()
    )

    fun updateSortOrder(sort: SortOrder) {
        sortOrder.update { sort }
    }

    fun setTimeZones(timeZones: List<TimeZone>) = viewModelScope.launch {
        timezoneRepository.replaceAll(*timeZones.toTypedArray())
    }

    fun deleteTimeZone(timeZone: TimeZone) = viewModelScope.launch {
        timezoneRepository.delete(timeZone = timeZone)
    }

    fun getDateWithOffset(timeZone: String): Pair<String, String> {
        val time = TimeHelper.getTimeByZone(timeZone)
        return TimeHelper.formatDateTime(time, false)
    }
}
