package com.bnyro.clock.ui.model

import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat.getBestDateTimePattern
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.postDelayed
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ClockModel : ViewModel() {
    private val handlerKey = "clockUpdateTimeHandler"
    private val handler = Handler(Looper.getMainLooper())
    var currentDate by mutableStateOf<Date>(Calendar.getInstance().time)
    private val datePattern: String = getBestDateTimePattern(Locale.getDefault(), "EE dd-MMM-yyyy")
    val dateFormatter: DateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
    val timeFormatter: DateFormat = DateFormat.getTimeInstance()

    val timeZones = TimeHelper.getAvailableTimeZones()
    var selectedTimeZones by mutableStateOf(
        Preferences.preferences.map { preferences ->
            preferences[Preferences.TIME_ZONES] ?: setOf()
        },
    )

    private fun updateTime() {
        currentDate = Calendar.getInstance().time
        handler.postDelayed(100, handlerKey, this::updateTime)
    }

    fun startLifecycle() {
        updateTime()
    }

    fun setTimeZones(timeZones: Set<String>) = viewModelScope.launch(Dispatchers.IO) {
        Preferences.instance.edit {
            it[Preferences.TIME_ZONES] = timeZones
        }
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
