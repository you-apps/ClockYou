package com.bnyro.clock.domain.usecase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.repository.AlarmRepository
import com.bnyro.clock.util.AlarmHelper

class CreateUpdateDeleteAlarmUseCase(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {
    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun createAlarm(alarm: Alarm) {
        // fixx maybe baby D:
        val newId = alarmRepository.addAlarm(alarm)
        val alarmWithId = alarm.copy(id = newId)
        AlarmHelper.enqueue(context, alarmWithId)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun updateAlarm(alarm: Alarm) {

        alarmRepository.updateAlarm(alarm)
        AlarmHelper.enqueue(context, alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {

        alarmRepository.deleteAlarm(alarm)
        AlarmHelper.cancel(context, alarm)
    }
}