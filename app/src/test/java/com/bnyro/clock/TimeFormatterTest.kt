package com.bnyro.clock

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.util.TimeHelper
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TimeFormatterTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var locale: Locale
    private lateinit var timeZone: TimeZone

    @Before
    fun setUp() {
        locale = Locale.getDefault()
        timeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        Settings.System.putString(
            context.contentResolver,
            Settings.System.TIME_12_24,
            null
        )
        Locale.setDefault(locale)
        TimeZone.setDefault(timeZone)
    }

    @Test
    fun displayedTimesFollowTwelveHourSystemSetting() {
        Settings.System.putString(
            context.contentResolver,
            Settings.System.TIME_12_24,
            "12"
        )

        assertEquals("12:00 AM", TimeHelper.millisToFormatted(context, 0))
        assertEquals(
            "1:05 PM",
            TimeHelper.millisToFormatted(context, (13 * 60L + 5) * 60 * 1000)
        )
        assertEquals(
            "1:05:09 PM",
            TimeHelper.formatDateTime(
                context,
                ZonedDateTime.of(2026, 8, 13, 13, 5, 9, 0, ZoneId.of("UTC")),
                true
            ).second
        )
    }

    @Test
    fun displayedTimesFollowTwentyFourHourSystemSetting() {
        Settings.System.putString(
            context.contentResolver,
            Settings.System.TIME_12_24,
            "24"
        )

        assertEquals("00:00", TimeHelper.millisToFormatted(context, 0))
        assertEquals(
            "13:05",
            TimeHelper.millisToFormatted(context, (13 * 60L + 5) * 60 * 1000)
        )
        assertEquals(
            "13:05:09",
            TimeHelper.formatDateTime(
                context,
                ZonedDateTime.of(2026, 8, 13, 13, 5, 9, 0, ZoneId.of("UTC")),
                true
            ).second
        )
    }

    @Test
    fun displayedWorldClockPreservesItsTimeZone() {
        Settings.System.putString(
            context.contentResolver,
            Settings.System.TIME_12_24,
            "24"
        )

        assertEquals(
            "22:05",
            TimeHelper.formatTime(
                context,
                ZonedDateTime.of(2026, 8, 13, 13, 5, 0, 0, ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Tokyo"))
            )
        )
    }
}
