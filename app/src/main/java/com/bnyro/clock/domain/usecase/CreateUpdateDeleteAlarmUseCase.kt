package com.bnyro.clock.domain.usecase

import android.content.Context
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.repository.AlarmRepository
import com.bnyro.clock.util.AlarmHelper

class CreateUpdateDeleteAlarmUseCase(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {
    suspend fun createAlarm(alarm: Alarm) {
        AlarmHelper.enqueue(context, alarm)
        alarmRepository.addAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        AlarmHelper.enqueue(context, alarm)
        alarmRepository.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        AlarmHelper.cancel(context, alarm)
        alarmRepository.deleteAlarm(alarm)
    }
}