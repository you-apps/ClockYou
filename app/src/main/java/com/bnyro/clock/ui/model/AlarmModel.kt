package com.bnyro.clock.ui.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AlarmModel : ViewModel() {
    var selectedAlarm: Alarm? by mutableStateOf(null)
    var alarms =
        runBlocking {
            DatabaseHolder.instance.alarmsDao().getAll()
        }.toMutableStateList()

    fun createAlarm(alarm: Alarm) {
        alarms.add(alarm)
        viewModelScope.launch(Dispatchers.IO) {
            alarm.id = DatabaseHolder.instance.alarmsDao().insert(alarm)
        }
    }

    fun updateAlarm(context: Context, alarm: Alarm) {
        AlarmHelper.enqueue(context, alarm)
        alarms.removeIf { it.id == alarm.id }
        alarms.add(alarm)
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().update(alarm)
        }
    }

    fun deleteAlarm(context: Context, alarm: Alarm) {
        AlarmHelper.cancel(context, alarm)
        alarms.removeIf { it.id == alarm.id }
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().delete(alarm)
        }
    }
}
