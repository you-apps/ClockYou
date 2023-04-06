package com.bnyro.clock.util

import java.util.*

object TimeHelper {
    fun getAvailableTimeZones(): List<com.bnyro.clock.obj.TimeZone> {
        return TimeZone.getAvailableIDs().distinct().mapNotNull {
            val zone = TimeZone.getTimeZone(it)
            getDisplayName(it)?.let { displayName ->
                com.bnyro.clock.obj.TimeZone(it, displayName, zone.rawOffset)
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

    fun getOffset(): Int {
        val mCalendar: Calendar = GregorianCalendar()
        return mCalendar.timeZone.rawOffset
    }
}
