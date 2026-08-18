package com.bnyro.clock

import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.util.AlarmHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AlarmHelperTest {
    @Test
    fun dismissedRepeatingAlarmAdvancesFromTheDismissedOccurrence() {
        val alarmTime = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
        }
        val alarm = Alarm(
            time = (alarmTime.get(Calendar.HOUR_OF_DAY) * 60L + alarmTime.get(Calendar.MINUTE)) * 60_000L,
            enabled = true,
            repeat = true
        )
        val dismissedAt = AlarmHelper.getAlarmTime(alarm)
        alarm.dismissedAt = dismissedAt

        val expected = Calendar.getInstance().apply {
            timeInMillis = dismissedAt
            add(Calendar.DATE, 1)
        }.timeInMillis

        assertEquals(expected, AlarmHelper.getAlarmTime(alarm))
    }
}
