package com.bnyro.clock

import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.RepeatAnchor
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.util.AlarmHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

class AlarmHelperTest {
    private fun recurringAlarm(
        startDate: LocalDate,
        repeatUnit: RepeatUnit,
        repeatInterval: Int = 1,
        repeatAnchor: RepeatAnchor = RepeatAnchor.DAY_OF_MONTH,
        endDate: LocalDate? = null,
        endOccurrences: Int? = null
    ) = Alarm(
        time = 8 * 60 * 60 * 1000L,
        enabled = true,
        startDate = startDate.toEpochDay(),
        repeatUnit = repeatUnit,
        repeatInterval = repeatInterval,
        repeatAnchor = repeatAnchor,
        endDate = endDate?.toEpochDay(),
        endOccurrences = endOccurrences
    )

    @Test
    fun dismissedRepeatingAlarmAdvancesFromTheDismissedOccurrence() {
        val alarmTime = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
        }
        val alarm = Alarm(
            time = (alarmTime.get(Calendar.HOUR_OF_DAY) * 60L + alarmTime.get(Calendar.MINUTE)) * 60_000L,
            enabled = true
        )
        val dismissedAt = AlarmHelper.getAlarmTime(alarm)
        alarm.dismissedAt = dismissedAt

        val expected = Calendar.getInstance().apply {
            timeInMillis = dismissedAt
            add(Calendar.DATE, 1)
        }.timeInMillis

        assertEquals(expected, AlarmHelper.getAlarmTime(alarm))
    }

    /**
     * Rings the current occurrence of the alarm the way the app does, by anchoring the alarm at
     * the occurrence it just rang and dismissing it.
     *
     * @return the occurrence the alarm rings next.
     */
    private fun ringOccurrence(alarm: Alarm): LocalDate {
        alarm.startDate = AlarmHelper.getNextOccurrence(alarm).toEpochDay()
        alarm.dismissedAt = AlarmHelper.getAlarmTime(alarm)
        return AlarmHelper.getNextOccurrence(alarm)
    }

    @Test
    fun dailyAlarmSkipsTheDaysOfItsInterval() {
        val startDate = LocalDate.of(2099, 1, 1)
        val alarm = recurringAlarm(startDate, RepeatUnit.DAY, repeatInterval = 3)

        assertEquals(startDate, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 1, 4), ringOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 1, 7), ringOccurrence(alarm))
    }

    @Test
    fun weeklyAlarmRingsOnEveryChosenDayOfItsIntervalWeeks() {
        val monday = LocalDate.of(2099, 1, 5)
        val alarm = recurringAlarm(monday, RepeatUnit.WEEK, repeatInterval = 2).apply {
            days = listOf(1, 3)
        }

        assertEquals(monday, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 1, 7), ringOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 1, 19), ringOccurrence(alarm))
    }

    @Test
    fun monthlyAlarmKeepsTheDayOfTheMonthOfItsStartDate() {
        val startDate = LocalDate.of(2099, 1, 31)
        val alarm = recurringAlarm(startDate, RepeatUnit.MONTH)

        assertEquals(startDate, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 2, 28), ringOccurrence(alarm))
    }

    @Test
    fun monthlyAlarmKeepsTheWeekdayOfItsStartDate() {
        val thirdMonday = LocalDate.of(2099, 1, 19)
        val alarm = recurringAlarm(
            thirdMonday,
            RepeatUnit.MONTH,
            repeatAnchor = RepeatAnchor.DAY_OF_WEEK
        )

        assertEquals(thirdMonday, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 2, 16), ringOccurrence(alarm))
        assertEquals(LocalDate.of(2099, 3, 16), ringOccurrence(alarm))
    }

    @Test
    fun yearlyAlarmKeepsTheDateOfItsStartDate() {
        val startDate = LocalDate.of(2099, 3, 5)
        val alarm = recurringAlarm(startDate, RepeatUnit.YEAR)

        assertEquals(startDate, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2100, 3, 5), ringOccurrence(alarm))
    }

    @Test
    fun yearlyAlarmKeepsTheWeekdayOfItsStartDate() {
        val thirdMondayOfAugust = LocalDate.of(2099, 8, 17)
        val alarm = recurringAlarm(
            thirdMondayOfAugust,
            RepeatUnit.YEAR,
            repeatAnchor = RepeatAnchor.DAY_OF_WEEK
        )

        assertEquals(thirdMondayOfAugust, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2100, 8, 16), ringOccurrence(alarm))
        assertEquals(LocalDate.of(2101, 8, 15), ringOccurrence(alarm))
    }

    @Test
    fun yearlyAlarmSkipsTheYearsOfItsInterval() {
        val startDate = LocalDate.of(2099, 3, 5)
        val alarm = recurringAlarm(startDate, RepeatUnit.YEAR, repeatInterval = 2)

        assertEquals(startDate, AlarmHelper.getNextOccurrence(alarm))
        assertEquals(LocalDate.of(2101, 3, 5), ringOccurrence(alarm))
    }

    @Test
    fun recurrenceEndsAfterTheLastAllowedOccurrence() {
        val today = LocalDate.now()
        val pending = recurringAlarm(today, RepeatUnit.DAY, endOccurrences = 5)
        assertFalse(AlarmHelper.hasRecurrenceEnded(pending))

        val rungOut = recurringAlarm(today.minusDays(5), RepeatUnit.DAY, endOccurrences = 2)
        assertTrue(AlarmHelper.hasRecurrenceEnded(rungOut))
    }

    @Test
    fun recurrenceEndsAfterTheChosenEndDate() {
        val today = LocalDate.now()
        val pending = recurringAlarm(today, RepeatUnit.DAY, endDate = today.plusDays(5))
        assertFalse(AlarmHelper.hasRecurrenceEnded(pending))

        val rungOut = recurringAlarm(today.minusDays(5), RepeatUnit.DAY, endDate = today.minusDays(1))
        assertTrue(AlarmHelper.hasRecurrenceEnded(rungOut))
    }
}
