package com.bnyro.clock.util.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.DigitalClockWidgetOptions

fun Context.saveDigitalClockWidgetSettings(
    appWidgetId: Int,
    options: DigitalClockWidgetOptions
) = widgetPreferences.edit {
    putBoolean(PREF_SHOW_DATE + appWidgetId, options.showDate)
    putBoolean(PREF_SHOW_TIME + appWidgetId, options.showTime)
    putFloat(PREF_DATE_TEXT_SIZE + appWidgetId, options.dateTextSize)
    putFloat(PREF_TIME_TEXT_SIZE + appWidgetId, options.timeTextSize)
    putString(PREF_TIME_ZONE + appWidgetId, options.timeZone)
    putString(PREF_TIME_ZONE_NAME + appWidgetId, options.timeZoneName)
    putBoolean(PREF_SHOW_BACKGROUND + appWidgetId, options.showBackground)
}

fun Context.loadDigitalClockWidgetSettings(
    appWidgetId: Int
): DigitalClockWidgetOptions = with(widgetPreferences) {

    val showDate = getBoolean(
        PREF_SHOW_DATE + appWidgetId,
        true
    )
    val showTime = getBoolean(
        PREF_SHOW_TIME + appWidgetId,
        true
    )

    val dateTextSize = getFloat(
        PREF_DATE_TEXT_SIZE + appWidgetId,
        DigitalClockWidgetOptions.DEFAULT_DATE_TEXT_SIZE
    )

    val timeTextSize = getFloat(
        PREF_TIME_TEXT_SIZE + appWidgetId,
        DigitalClockWidgetOptions.DEFAULT_TIME_TEXT_SIZE
    )

    val timeZone = getString(
        PREF_TIME_ZONE + appWidgetId,
        null
    )

    val timeZoneName = getString(
        PREF_TIME_ZONE_NAME + appWidgetId,
        ""
    ) ?: ""
    val showBackground = getBoolean(
        PREF_SHOW_BACKGROUND + appWidgetId,
        true
    )

    return DigitalClockWidgetOptions(
        showDate = showDate,
        showTime = showTime,
        dateTextSize = dateTextSize,
        timeTextSize = timeTextSize,
        timeZone = timeZone,
        timeZoneName = timeZoneName,
        showBackground = showBackground
    )
}

fun Context.deleteDigitalClockWidgetPref(appWidgetId: Int) =
    widgetPreferences.edit {
        remove(PREF_SHOW_DATE + appWidgetId)
        remove(PREF_SHOW_TIME + appWidgetId)
        remove(PREF_SHOW_BACKGROUND + appWidgetId)
        remove(PREF_DATE_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_ZONE + appWidgetId)
        remove(PREF_TIME_ZONE_NAME + appWidgetId)
    }


fun Context.updateDigitalClockWidget(
    appWidgetId: Int,
    options: DigitalClockWidgetOptions
) {
    val appWidgetManager = AppWidgetManager.getInstance(this)
    val views = RemoteViews(packageName, R.layout.digital_clock)
    views.applyDigitalClockWidgetOptions(options)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun RemoteViews.applyDigitalClockWidgetOptions(
    options: DigitalClockWidgetOptions
) {
    val dateVisibility = when (options.showDate) {
        true -> View.VISIBLE
        false -> View.GONE
    }
    val timeVisibility = when (options.showTime) {
        true -> View.VISIBLE
        false -> View.GONE
    }
    val timeZoneVisibility = when (options.timeZone) {
        null -> View.GONE
        else -> View.VISIBLE
    }

    val backgroundResource = when (options.showBackground) {
        true -> R.drawable.widget_shape
        false -> 0
    }

    setViewVisibility(R.id.textClock, dateVisibility)
    setViewVisibility(R.id.textClock2, timeVisibility)

    setTextViewTextSize(R.id.textClock, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)
    setTextViewTextSize(R.id.cityName, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize - 4)
    setTextViewTextSize(R.id.textClock2, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)

    setString(R.id.textClock, "setTimeZone", options.timeZone)
    setString(R.id.textClock2, "setTimeZone", options.timeZone)

    setInt(R.id.frameLayout, "setBackgroundResource", backgroundResource)

    setViewVisibility(R.id.cityName, timeZoneVisibility)
    setTextViewText(R.id.cityName, options.timeZoneName)
}