package com.bnyro.clock.domain.usecase

import android.content.Context
import com.bnyro.clock.data.database.DatabaseHolder
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateUpdateDeleteAlarmUseCase(private val context: Context) {
    suspend fun createAlarm(alarm: Alarm) {
        alarm.enabled = true
        AlarmHelper.enqueue(context, alarm)
        withContext(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().insert(alarm)
        }
    }

    suspend fun updateAlarm(alarm: Alarm) {
        AlarmHelper.enqueue(context, alarm)
        withContext(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().update(alarm)
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        AlarmHelper.cancel(context, alarm)
        withContext(Dispatchers.IO) {
            DatabaseHolder.instance.alarmsDao().delete(alarm)
        }
    }
}