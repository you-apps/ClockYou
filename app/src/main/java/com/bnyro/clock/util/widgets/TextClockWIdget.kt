package com.bnyro.clock.util.widgets

import android.content.Context
import androidx.core.content.edit
import com.bnyro.clock.domain.model.ClockWidgetOptions

fun Context.saveClockWidgetSettings(
    appWidgetId: Int,
    options: ClockWidgetOptions
) = widgetPreferences.edit {
    putBoolean(PREF_SHOW_DATE + appWidgetId, options.showDate)
    putBoolean(PREF_SHOW_TIME + appWidgetId, options.showTime)
    putFloat(PREF_DATE_TEXT_SIZE + appWidgetId, options.dateTextSize)
    putFloat(PREF_TIME_TEXT_SIZE + appWidgetId, options.timeTextSize)
    putString(PREF_TIME_ZONE + appWidgetId, options.timeZone)
    putString(PREF_TIME_ZONE_NAME + appWidgetId, options.timeZoneName)
    putBoolean(PREF_SHOW_BACKGROUND + appWidgetId, options.showBackground)
}

fun Context.loadClockWidgetSettings(
    appWidgetId: Int, defaultClockWidgetOptions: ClockWidgetOptions)
: ClockWidgetOptions = with(widgetPreferences) {
    val showDate = getBoolean(
        PREF_SHOW_DATE + appWidgetId,
        defaultClockWidgetOptions.showDate
    )
    val showTime = getBoolean(
        PREF_SHOW_TIME + appWidgetId,
        defaultClockWidgetOptions.showTime
    )

    val dateTextSize = getFloat(
        PREF_DATE_TEXT_SIZE + appWidgetId,
        defaultClockWidgetOptions.dateTextSize
    )

    val timeTextSize = getFloat(
        PREF_TIME_TEXT_SIZE + appWidgetId,
        defaultClockWidgetOptions.timeTextSize
    )

    val timeZone = getString(
        PREF_TIME_ZONE + appWidgetId,
        defaultClockWidgetOptions.timeZone
    )

    val timeZoneName = getString(
        PREF_TIME_ZONE_NAME + appWidgetId,
        defaultClockWidgetOptions.timeZoneName
    ) ?: defaultClockWidgetOptions.timeZoneName
    val showBackground = getBoolean(
        PREF_SHOW_BACKGROUND + appWidgetId,
        defaultClockWidgetOptions.showBackground
    )

    return ClockWidgetOptions(
        showDate = showDate,
        showTime = showTime,
        dateTextSize = dateTextSize,
        timeTextSize = timeTextSize,
        timeZone = timeZone,
        timeZoneName = timeZoneName,
        showBackground = showBackground
    )
}

fun Context.deleteClockWidgetPref(appWidgetId: Int) =
    widgetPreferences.edit {
        remove(PREF_SHOW_DATE + appWidgetId)
        remove(PREF_SHOW_TIME + appWidgetId)
        remove(PREF_SHOW_BACKGROUND + appWidgetId)
        remove(PREF_DATE_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_ZONE + appWidgetId)
        remove(PREF_TIME_ZONE_NAME + appWidgetId)
    }
