package com.bnyro.clock.domain.model

import androidx.annotation.StringRes
import com.bnyro.clock.R
import com.bnyro.clock.util.AlarmHelper

enum class AlarmSortOrder(@StringRes val value: Int) {
    HOUR_OF_DAY(R.string.hours),
    LABEL(R.string.label),
    WEEKDAY(R.string.weekdays),
    UPCOMING(R.string.upcoming);

    fun sort(alarms: List<Alarm>): List<Alarm> = when (this) {
        HOUR_OF_DAY -> alarms.sortedBy { it.time }
        LABEL -> alarms.sortedBy { it.label }
        WEEKDAY -> alarms.sortedBy { it.days.firstOrNull() }
        UPCOMING -> alarms.map { alarm ->
            val nextOccurrence = if (alarm.enabled && !AlarmHelper.hasRecurrenceEnded(alarm)) {
                AlarmHelper.getAlarmTime(alarm)
            } else {
                null
            }
            alarm to (nextOccurrence ?: Long.MAX_VALUE)
        }.sortedBy { it.second }.map { it.first }
    }
}
