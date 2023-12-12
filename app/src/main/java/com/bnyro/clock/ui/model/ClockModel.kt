package com.bnyro.clock.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.obj.TimeZone
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ClockModel : ViewModel() {
    private val sortOrderPref =
        Preferences.instance.getString(Preferences.clockSortOrder, "").orEmpty()
    var sortOrder: SortOrder =
        if (sortOrderPref.isNotEmpty()) SortOrder.valueOf(sortOrderPref) else SortOrder.ALPHABETIC

    var sortedZones by mutableStateOf(listOf<TimeZone>())
    val timeZones = TimeHelper.getAvailableTimeZones()
    var selectedTimeZones = runBlocking {
        DatabaseHolder.instance.timeZonesDao().getAll()
    }

    init {
        updateSortOrder()
    }

    fun updateSortOrder(sort: SortOrder? = null) {
        sort?.let {
            sortOrder = it
        }
        val zones = selectedTimeZones.distinct()
        sortedZones = when (sortOrder) {
            SortOrder.ALPHABETIC -> zones.sortedBy { it.displayName }
            SortOrder.OFFSET -> zones.sortedBy { it.offset }
        }
    }

    fun setTimeZones(timeZones: List<TimeZone>) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        selectedTimeZones = timeZones
        updateSortOrder()
        DatabaseHolder.instance.timeZonesDao().clear()
        DatabaseHolder.instance.timeZonesDao().insertAll(*timeZones.toTypedArray())
    }

    fun getDateWithOffset(timeZone: String): Pair<String, String> {
        val time = TimeHelper.getTimeByZone(timeZone)
        return TimeHelper.formatDateTime(time, false)
    }
}
