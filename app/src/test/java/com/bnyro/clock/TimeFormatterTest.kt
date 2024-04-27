package com.bnyro.clock

import com.bnyro.clock.util.TimeHelper.millisToFormatted
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TimeFormatterTest {

    @Test
    fun millisToFormatted_convertsMillisToFormattedTime() {
        // Test case 1: Midnight
        val millis1 = 0L
        val formattedTime1 = millisToFormatted(millis1)
        val expectedTime1 =
            LocalTime.MIDNIGHT.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        assertEquals(expectedTime1, formattedTime1)

        // Test case 2: Afternoon time
        val millis2 = 12 * 60 * 60 * 1000L // 12:00:00 PM
        val formattedTime2 = millisToFormatted(millis2)
        val expectedTime2 =
            LocalTime.NOON.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        assertEquals(expectedTime2, formattedTime2)

        // Test case 3: Time with milliseconds
        val millis3 = ((10 * 60 + 33) * 60 + 54) * 1000L + 567L // 10:33:54.567 AM
        val formattedTime3 = millisToFormatted(millis3)
        val expectedTime3 = LocalTime.of(10, 33, 54, 567_000_000)
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        assertEquals(expectedTime3, formattedTime3)
    }
}