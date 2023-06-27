package com.bnyro.clock.ui.model

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.postDelayed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ClockModel : ViewModel() {
    private val handlerKey = "clockUpdateTimeHandler"
    private val handler = Handler(Looper.getMainLooper())
    var currentDate by mutableStateOf<Date>(Calendar.getInstance().time)
    private val sortOrderPref = Preferences.instance.getString(Preferences.clockSortOrder, "").orEmpty()
    var sortOrder by mutableStateOf(
        if (sortOrderPref.isNotEmpty()) SortOrder.valueOf(sortOrderPref) else SortOrder.ALPHABETIC
    )

    val timeZones = TimeHelper.getAvailableTimeZones()
    var selectedTimeZones by mutableStateOf(
        runBlocking {
            DatabaseHolder.instance.timeZonesDao().getAll()
        }
    )

    private fun updateTime() {
        currentDate = TimeHelper.currentTime
        handler.postDelayed(100, handlerKey, this::updateTime)
    }

    fun startLifecycle() {
        updateTime()
    }

    fun setTimeZones(timeZones: List<com.bnyro.clock.obj.TimeZone>) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        selectedTimeZones = timeZones
        DatabaseHolder.instance.timeZonesDao().clear()
        DatabaseHolder.instance.timeZonesDao().insertAll(*timeZones.toTypedArray())
    }

    fun stopLifecycle() {
        handler.removeCallbacksAndMessages(handlerKey)
    }

    fun getDateWithOffset(date: Date, offset: Int): Date {
        val currentOffset = TimeHelper.getOffset()
        val calendar = GregorianCalendar()
        calendar.time = date
        calendar.add(Calendar.MILLISECOND, offset - currentOffset)
        return calendar.time
    }
}
