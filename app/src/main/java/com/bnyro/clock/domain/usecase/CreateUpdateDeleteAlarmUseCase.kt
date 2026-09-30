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
        alarm.snoozedUntil = null
        alarm.dismissedAt = null
        alarm.startDate = AlarmHelper.getNextRepetitionStart(alarm)?.toEpochDay() ?: alarm.startDate
        if (AlarmHelper.hasRecurrenceEnded(alarm)) alarm.enabled = false
        val newId = alarmRepository.addAlarm(alarm)
        val alarmWithId = alarm.copy(id = newId)
        AlarmHelper.enqueue(context, alarmWithId)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun updateAlarm(alarm: Alarm) {
        alarm.snoozedUntil = null
        alarm.dismissedAt = null
        alarm.startDate = AlarmHelper.getNextRepetitionStart(alarm)?.toEpochDay() ?: alarm.startDate
        if (AlarmHelper.hasRecurrenceEnded(alarm)) alarm.enabled = false
        alarmRepository.updateAlarm(alarm)
        AlarmHelper.enqueue(context, alarm)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun dismissUpcomingAlarm(alarm: Alarm) {
        if (alarm.snoozedUntil == null &&
            alarm.dismissedAt?.let { it > System.currentTimeMillis() } == true
        ) return

        AlarmHelper.cancel(context, alarm)

        if (alarm.snoozedUntil == null) {
            alarm.dismissedAt = AlarmHelper.getAlarmTime(alarm)
        }
        alarm.snoozedUntil = null
        if (AlarmHelper.hasRecurrenceEnded(alarm)) alarm.enabled = false
        alarmRepository.updateAlarm(alarm)
        AlarmHelper.enqueue(context, alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {

        alarmRepository.deleteAlarm(alarm)
        AlarmHelper.cancel(context, alarm)
    }
}
