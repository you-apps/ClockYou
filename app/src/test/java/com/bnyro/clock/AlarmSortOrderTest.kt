package com.bnyro.clock

import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.util.AlarmHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AlarmSortOrderTest {
    @Test
    fun upcomingOrdersByOccurrenceInsteadOfClockTime() {
        val tomorrow = LocalDate.now().plusDays(1)
        val sooner = Alarm(time = 20 * 3_600_000L, enabled = true,
            startDate = tomorrow.toEpochDay(), repeatUnit = RepeatUnit.DAY)
        val later = sooner.copy(time = 6 * 3_600_000L, startDate = tomorrow.plusDays(1).toEpochDay())

        assertEquals(listOf(sooner, later), AlarmSortOrder.UPCOMING.sort(listOf(later, sooner)))
        assertEquals(listOf(later, sooner), AlarmSortOrder.HOUR_OF_DAY.sort(listOf(sooner, later)))
    }

    @Test
    fun upcomingPutsDisabledAndFinishedAlarmsLast() {
        val tomorrow = LocalDate.now().plusDays(1)
        val active = Alarm(time = 8 * 3_600_000L, enabled = true,
            startDate = tomorrow.toEpochDay(), repeatUnit = RepeatUnit.DAY)
        val disabled = active.copy(enabled = false)
        val ended = active.copy(endDate = tomorrow.minusDays(1).toEpochDay())
        val exhausted = active.copy(startDate = tomorrow.minusDays(10).toEpochDay(), endOccurrences = 1)
        val silent = active.copy(repeatUnit = RepeatUnit.WEEK, days = emptyList())

        assertEquals(listOf(active, disabled, ended, exhausted, silent),
            AlarmSortOrder.UPCOMING.sort(listOf(disabled, ended, exhausted, silent, active)))
    }

    @Test
    fun upcomingRespectsDismissalsAndWeeklyRecurrence() {
        val tomorrow = LocalDate.now().plusDays(1)
        val daily = Alarm(time = 8 * 3_600_000L, enabled = true,
            startDate = tomorrow.toEpochDay(), repeatUnit = RepeatUnit.DAY)
        val dismissed = daily.copy(dismissedAt = AlarmHelper.getAlarmTime(daily))
        val weekly = daily.copy(repeatUnit = RepeatUnit.WEEK,
            days = listOf(tomorrow.plusDays(2).dayOfWeek.value % 7))

        assertEquals(listOf(daily, dismissed, weekly),
            AlarmSortOrder.UPCOMING.sort(listOf(weekly, dismissed, daily)))
    }

    @Test
    fun existingOrdersKeepTheirOriginalKeysAndStableTies() {
        val missing = Alarm(time = 1L, label = null, days = listOf(6))
        val upper = Alarm(time = 2L, label = "Work", days = listOf(0, 1))
        val lower = Alarm(time = 3L, label = "breakfast", days = listOf(1))
        val tie = lower.copy(time = 4L)
        val alarms = listOf(lower, upper, missing, tie)

        assertEquals(listOf(missing, upper, lower, tie), AlarmSortOrder.LABEL.sort(alarms))
        assertEquals(listOf(upper, lower, tie, missing), AlarmSortOrder.WEEKDAY.sort(alarms))
        assertEquals(listOf(missing, upper, lower, tie), AlarmSortOrder.HOUR_OF_DAY.sort(alarms))
    }
}
