package com.bnyro.clock.util

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import com.bnyro.clock.R


private const val PREF_FILE = "WidgetConfig"
private const val PREF_SHOW_DATE = "showDate:"
private const val PREF_SHOW_TIME = "showTime:"
private const val PREF_SHOW_BACKGROUND = "showBackground:"
private const val PREF_DATE_TEXT_SIZE = "dateTextSize:"
private const val PREF_TIME_TEXT_SIZE = "timeTextSize:"
private const val PREF_TIME_ZONE = "timeZone:"
private const val PREF_TIME_ZONE_NAME = "timeZoneName:"

data class DigitalClockWidgetOptions(
    var showDate: Boolean = true,
    var showTime: Boolean = true,
    var dateTextSize: Float = DEFAULT_DATE_TEXT_SIZE,
    var timeTextSize: Float = DEFAULT_TIME_TEXT_SIZE,
    var timeZone: String? = null,
    var timeZoneName: String = "",
    var showBackground: Boolean = true
) {
    companion object {
        const val DEFAULT_DATE_TEXT_SIZE = 16f
        const val DEFAULT_TIME_TEXT_SIZE = 52f

        val dateSizeOptions = listOf(
            12f,
            16f,
            20f,
            24f,
            28f,
            32f
        )

        val timeSizeOptions = listOf(
            36f,
            40f,
            44f,
            48f,
            52f,
            56f,
            60f,
            64f,
            68f,
            72f,
            76f,
            80f
        )
    }
}

private val Context.widgetPreferences
    get() = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

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