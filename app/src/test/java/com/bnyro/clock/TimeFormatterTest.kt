package com.bnyro.clock

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.util.TimeHelper
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.time.Duration.Companion.minutes
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

    @Test
    fun translatedDurationsKeepTheirQuantityAndUnitLabels() {
        for ((language, unit, durations) in listOf(
            Triple("pl", "Minuty", listOf("1 minuta", "2 minuty", "5 minut")),
            Triple("ca", "Minuts", listOf("1 minut", "2 minuts", "5 minuts")),
            Triple("zh-CN", "分钟", listOf("1分钟", "2分钟", "5分钟"))
        )) {
            val translated = context.createConfigurationContext(
                Configuration(context.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(language))
                }
            )
            assertEquals(unit, translated.getString(R.string.minutes))
            for ((quantity, expected) in listOf(1, 2, 5).zip(durations)) {
                assertEquals(expected, TimeHelper.durationToFormatted(translated, quantity.minutes))
            }
        }
    }

    @Test
    fun everyLocaleProvidesUnitLabelsAndAllDurationQuantities() {
        for (language in context.resources.assets.locales) {
            val translated = context.createConfigurationContext(
                Configuration(context.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(language))
                }
            )
            assertTrue(language, translated.getString(R.string.minutes).isNotBlank())
            for (resource in listOf(R.plurals.duration_days, R.plurals.duration_hours, R.plurals.duration_minutes)) {
                for (quantity in listOf(0, 1, 2, 3, 5, 11, 21, 100)) {
                    assertTrue(language, translated.resources.getQuantityString(resource, quantity, quantity).isNotBlank())
                }
            }
        }
    }
}
