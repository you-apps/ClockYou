package com.bnyro.clock.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AlarmModel : ViewModel() {
    var alarms by mutableStateOf(
        runBlocking {
            DatabaseHolder.instance.alarmsDao().getAll()
        },
    )

    fun createAlarm(alarm: Alarm) {
        alarms = alarms + alarm
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().insertAll(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().update(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        alarms = alarms.filter { it.id != alarm.id }
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().delete(alarm)
        }
    }
}
