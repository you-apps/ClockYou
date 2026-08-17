package com.bnyro.clock

import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.RepeatAnchor
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.util.AlarmHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        repeatDuration: Int? = null,
        repeatDurationUnit: RepeatUnit = RepeatUnit.DAY,
        endDate: LocalDate? = null,
        endOccurrences: Int? = null
    ) = Alarm(
        time = 8 * 60 * 60 * 1000L,
        enabled = true,
        startDate = startDate.toEpochDay(),
        repeatUnit = repeatUnit,
        repeatInterval = repeatInterval,
        repeatAnchor = repeatAnchor,
        repeatDuration = repeatDuration,
        repeatDurationUnit = repeatDurationUnit,
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
        val dismissedAt = AlarmHelper.getAlarmTime(alarm)!!
        alarm.dismissedAt = dismissedAt

        val expected = Calendar.getInstance().apply {
            timeInMillis = dismissedAt
            add(Calendar.DATE, 1)
        }.timeInMillis

        assertEquals(expected, AlarmHelper.getAlarmTime(alarm))
    }

    /**
     * @return the first [count] days the alarm rings on, from its start date onwards.
     */
    private fun occurrences(alarm: Alarm, count: Int): List<LocalDate> {
        val occurrences = mutableListOf<LocalDate>()
        var from = LocalDate.ofEpochDay(alarm.startDate)
        repeat(count) {
            val occurrence = AlarmHelper.occurrenceOnOrAfter(alarm, from) ?: return occurrences
            occurrences += occurrence
            from = occurrence.plusDays(1)
        }
        return occurrences
    }

    @Test
    fun dailyAlarmSkipsTheDaysOfItsInterval() {
        val alarm = recurringAlarm(LocalDate.of(2099, 1, 1), RepeatUnit.DAY, repeatInterval = 3)

        assertEquals(
            listOf(LocalDate.of(2099, 1, 1), LocalDate.of(2099, 1, 4), LocalDate.of(2099, 1, 7)),
            occurrences(alarm, 3)
        )
    }

    @Test
    fun weeklyAlarmRingsOnEveryChosenDayOfItsIntervalWeeks() {
        val alarm = recurringAlarm(LocalDate.of(2099, 1, 5), RepeatUnit.WEEK, repeatInterval = 2)
            .apply { days = listOf(1, 3) }

        assertEquals(
            listOf(LocalDate.of(2099, 1, 5), LocalDate.of(2099, 1, 7), LocalDate.of(2099, 1, 19)),
            occurrences(alarm, 3)
        )
    }

    @Test
    fun monthlyAlarmKeepsTheDayOfTheMonthOfItsStartDate() {
        val alarm = recurringAlarm(LocalDate.of(2099, 1, 31), RepeatUnit.MONTH)

        assertEquals(
            listOf(LocalDate.of(2099, 1, 31), LocalDate.of(2099, 2, 28), LocalDate.of(2099, 3, 31)),
            occurrences(alarm, 3)
        )
    }

    @Test
    fun monthlyAlarmKeepsTheWeekdayOfItsStartDate() {
        val alarm = recurringAlarm(
            LocalDate.of(2099, 1, 19),
            RepeatUnit.MONTH,
            repeatAnchor = RepeatAnchor.DAY_OF_WEEK
        )

        assertEquals(
            listOf(LocalDate.of(2099, 1, 19), LocalDate.of(2099, 2, 16), LocalDate.of(2099, 3, 16)),
            occurrences(alarm, 3)
        )
    }

    @Test
    fun yearlyAlarmKeepsTheDateOfItsStartDate() {
        val alarm = recurringAlarm(LocalDate.of(2099, 3, 5), RepeatUnit.YEAR)

        assertEquals(
            listOf(LocalDate.of(2099, 3, 5), LocalDate.of(2100, 3, 5)),
            occurrences(alarm, 2)
        )
    }

    @Test
    fun yearlyAlarmKeepsTheWeekdayOfItsStartDate() {
        val alarm = recurringAlarm(
            LocalDate.of(2099, 8, 17),
            RepeatUnit.YEAR,
            repeatAnchor = RepeatAnchor.DAY_OF_WEEK
        )

        assertEquals(
            listOf(LocalDate.of(2099, 8, 17), LocalDate.of(2100, 8, 16), LocalDate.of(2101, 8, 15)),
            occurrences(alarm, 3)
        )
    }

    @Test
    fun yearlyAlarmSkipsTheYearsOfItsInterval() {
        val alarm = recurringAlarm(LocalDate.of(2099, 3, 5), RepeatUnit.YEAR, repeatInterval = 2)

        assertEquals(
            listOf(LocalDate.of(2099, 3, 5), LocalDate.of(2101, 3, 5)),
            occurrences(alarm, 2)
        )
    }

    @Test
    fun repetitionKeepsRingingForTheDaysItLastsFor() {
        val alarm = recurringAlarm(
            LocalDate.of(2099, 8, 5),
            RepeatUnit.DAY,
            repeatInterval = 6,
            repeatDuration = 2
        )

        assertEquals(
            listOf(5, 6, 11, 12, 17, 18).map { LocalDate.of(2099, 8, it) },
            occurrences(alarm, 6)
        )
    }

    @Test
    fun weeklyRepetitionOnlyRingsOnChosenDaysThatFallWithinItsRun() {
        val monday = LocalDate.of(2099, 8, 17)
        val alarm = recurringAlarm(monday, RepeatUnit.WEEK, repeatDuration = 2).apply {
            days = listOf(5, 6)
        }

        assertNull(AlarmHelper.getNextOccurrence(alarm))
        assertFalse(AlarmHelper.hasRecurrenceEnded(alarm))
    }

    @Test
    fun aRunLongerThanAWeekReachesEveryChosenDay() {
        val alarm = recurringAlarm(
            LocalDate.of(2099, 8, 17),
            RepeatUnit.WEEK,
            repeatDuration = 1,
            repeatDurationUnit = RepeatUnit.WEEK
        ).apply { days = listOf(5, 6) }

        assertEquals(
            listOf(LocalDate.of(2099, 8, 21), LocalDate.of(2099, 8, 22)),
            occurrences(alarm, 2)
        )
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
