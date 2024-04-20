package com.bnyro.clock.presentation.screens.clock.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.data.database.DatabaseHolder
import com.bnyro.clock.domain.model.SortOrder
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import com.bnyro.clock.util.getCountryTimezones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClockModel(application: Application) : AndroidViewModel(application) {
    private val sortOrderPref =
        Preferences.instance.getString(Preferences.clockSortOrder, "").orEmpty()
    val sortOrder =
        MutableStateFlow(if (sortOrderPref.isNotEmpty()) SortOrder.valueOf(sortOrderPref) else SortOrder.ALPHABETIC)
    private val countryTimezones = getCountryTimezones(application.applicationContext)
    val timeZones = TimeHelper.getTimezonesForCountries(countryTimezones)
    var selectedTimeZones = combine(
        DatabaseHolder.instance.timeZonesDao().getAllStream(),
        sortOrder
    ) { selectedZones, sortOrder ->
        val zones = selectedZones.distinct()
        when (sortOrder) {
            SortOrder.ALPHABETIC -> zones.sortedBy { it.zoneName }
            SortOrder.OFFSET -> zones.sortedBy { it.offset }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = listOf()
    )

    fun updateSortOrder(sort: SortOrder) {
        sortOrder.update { sort }
    }

    fun setTimeZones(timeZones: List<TimeZone>) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        DatabaseHolder.instance.timeZonesDao().clear()
        DatabaseHolder.instance.timeZonesDao().insertAll(*timeZones.toTypedArray())
    }

    fun deleteTimeZone(timeZone: TimeZone) = viewModelScope.launch(Dispatchers.IO) {
        DatabaseHolder.instance.timeZonesDao().delete(timeZone = timeZone)
    }

    fun getDateWithOffset(timeZone: String): Pair<String, String> {
        val time = TimeHelper.getTimeByZone(timeZone)
        return TimeHelper.formatDateTime(time, false)
    }
}
