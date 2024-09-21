package com.bnyro.clock

import com.bnyro.clock.util.AlarmHelper
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

class SummerTimeTest {
    @Test
    fun scheduleWithSummerTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        val date = Date(Date.UTC(2024, 10, 30, 15, 0, 0))
        val calendar = GregorianCalendar()
        calendar.time = date
        val millis = calendar.timeInMillis
        AlarmHelper.fixDaylightTime(calendar)

        if (calendar.timeZone.inDaylightTime(calendar.time)) {
            assert(millis == calendar.timeInMillis + 60 * 60 * 1000L)
        } else {
            assert(millis == calendar.timeInMillis - 60 * 60 * 1000L)
        }
    }

    @Test
    fun scheduleWithoutDaylightDiff() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        val date = Date()
        val calendar = GregorianCalendar()
        calendar.time = date
        val millis = calendar.timeInMillis
        AlarmHelper.fixDaylightTime(calendar)

        assert(millis == calendar.timeInMillis)
    }
}