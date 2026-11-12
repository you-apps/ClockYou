package com.bnyro.clock.domain.usecase

import android.content.Context
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.repository.AlarmRepository
import com.bnyro.clock.util.AlarmHelper
import java.time.LocalDate

class CreateUpdateDeleteAlarmUseCase(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {
    suspend fun createAlarm(alarm: Alarm) {
        prepareForScheduling(alarm)
        val newId = alarmRepository.addAlarm(alarm)
        val alarmWithId = alarm.copy(id = newId)
        AlarmHelper.enqueue(context, alarmWithId)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        prepareForScheduling(alarm)
        alarmRepository.updateAlarm(alarm)
        AlarmHelper.enqueue(context, alarm)
    }

    fun prepareForScheduling(alarm: Alarm) {
        alarm.snoozedUntil = null
        alarm.dismissedAt = null
        if (!alarm.advanced) alarm.startDate = LocalDate.now().toEpochDay()
        alarm.startDate = AlarmHelper.getNextRepetitionStart(alarm)?.toEpochDay() ?: alarm.startDate
        if (AlarmHelper.hasRecurrenceEnded(alarm)) alarm.enabled = false
    }

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
