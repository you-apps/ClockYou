package com.bnyro.clock.util

import com.bnyro.clock.obj.TimeObject
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object TimeHelper {
    val currentTime: Date get() = Calendar.getInstance().time

    fun getAvailableTimeZones(): List<com.bnyro.clock.obj.TimeZone> {
        return TimeZone.getAvailableIDs().distinct().mapNotNull {
            val zone = TimeZone.getTimeZone(it)
            getDisplayName(it)?.let { displayName ->
                val offset = zone.getOffset(Calendar.getInstance().timeInMillis)
                com.bnyro.clock.obj.TimeZone(it, displayName, offset)
            }
        }.distinctBy { it.displayName }
            .sortedBy { it.displayName }
    }

    private fun getDisplayName(timeZone: String): String? {
        return timeZone
            .takeIf { it.none { c -> c.isDigit() } }
            ?.split("/", limit = 2)
            ?.takeIf { it.size > 1 }
            ?.let { "${it.last().replace("_", "")} (${it.first()})" }
    }

    fun getCurrentWeekDay(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }

    fun formatDateTime(time: ZonedDateTime): Pair<String, String> {
        val showSeconds = Preferences.instance.getBoolean(Preferences.showSecondsKey, true)
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

        val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
        var formattedTime = timeFormatter.format(time)

        if (!showSeconds) {
            formattedTime = formattedTime.let {
                it.removeRange(
                    Regex("\\d+:\\d+(:\\d+)").find(
                        it
                    )!!.groups[1]!!.range
                )
            }
        }
        return dateFormatter.format(time) to formattedTime
    }

    fun millisToFormatted(millis: Long): String {
        val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val localTime = LocalTime.of(
            millis.div(1000 * 60 * 60).toInt(),
            millis.div(1000 * 60).mod(60),
            millis.mod(1000)
        )
        return timeFormatter.format(localTime)
    }

    fun millisToTime(millis: Long): TimeObject {
        val hours = millis.div(1000 * 60 * 60).toInt()
        val minutes = millis.div(1000 * 60).mod(60)
        val seconds = millis.div(1000).mod(60)
        val milliseconds = millis.mod(1000)
        return TimeObject(hours, minutes, seconds, milliseconds)
    }

    fun getTimeByZone(timeZone: String? = null): ZonedDateTime {
        val zone = timeZone?.let { ZoneId.of(timeZone) } ?: ZoneId.systemDefault()
        val now = Instant.now()
        return now.atZone(zone)
    }
}
