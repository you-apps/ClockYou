package com.bnyro.clock.util.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.edit
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AnalogClockWidgetOptions
import com.bnyro.clock.ui.MainActivity

fun Context.saveAnalogClockWidgetSettings(
    appWidgetId: Int, options: AnalogClockWidgetOptions
) {
    widgetPreferences.edit {
        putInt(PREF_CLOCK_HOUR_HAND + appWidgetId, options.hourHand)
        putInt(PREF_CLOCK_MINUTE_HAND + appWidgetId, options.minuteHand)
        putInt(PREF_CLOCK_SECOND_HAND + appWidgetId, options.secondHand)
        putInt(PREF_CLOCK_DIAL + appWidgetId, options.dial)
        putString(PREF_CLOCK_FACE_NAME + appWidgetId, options.clockFaceName)
    }
}

fun Context.loadAnalogClockWidgetSettings(
    appWidgetId: Int
): AnalogClockWidgetOptions = with(widgetPreferences) {
    val hourHand = getInt(
        PREF_CLOCK_HOUR_HAND + appWidgetId, 0
    )

    val minuteHand = getInt(
        PREF_CLOCK_MINUTE_HAND + appWidgetId, 0
    )
    val secondHand = getInt(
        PREF_CLOCK_SECOND_HAND + appWidgetId, 0
    )
    val dial = getInt(
        PREF_CLOCK_DIAL + appWidgetId, 0
    )
    val clockFaceName = getString(
        PREF_CLOCK_FACE_NAME + appWidgetId, "Default"
    ) ?: "Default"

    return AnalogClockWidgetOptions(
        hourHand = hourHand,
        minuteHand = minuteHand,
        secondHand = secondHand,
        dial = dial,
        clockFaceName = clockFaceName
    )
}

fun Context.deleteAnalogClockWidgetPref(appWidgetId: Int) =
    widgetPreferences.edit {
        remove(PREF_CLOCK_HOUR_HAND + appWidgetId)
        remove(PREF_CLOCK_MINUTE_HAND + appWidgetId)
        remove(PREF_CLOCK_SECOND_HAND + appWidgetId)
        remove(PREF_CLOCK_DIAL + appWidgetId)
    }

fun Context.updateAnalogClockWidget(
    appWidgetId: Int, options: AnalogClockWidgetOptions
) {
    val appWidgetManager = AppWidgetManager.getInstance(this)
    val views = RemoteViews(packageName, R.layout.analog_clock)
    views.applyAnalogClockWidgetOptions(appWidgetId, options, this)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun RemoteViews.applyAnalogClockWidgetOptions(
    appWidgetId: Int, options: AnalogClockWidgetOptions, context: Context
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    setOnClickPendingIntent(R.id.analog_clock, pendingIntent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (options.dial != 0) {
            setIcon(
                R.id.analog_clock, "setDial", Icon.createWithResource(context, options.dial)
            )
        }
        if (options.hourHand != 0) {
            setIcon(
                R.id.analog_clock, "setHourHand", Icon.createWithResource(context, options.hourHand)
            )
        }
        if (options.minuteHand != 0) {
            setIcon(
                R.id.analog_clock,
                "setMinuteHand",
                Icon.createWithResource(context, options.minuteHand)
            )
        }
        if (options.secondHand != 0) {
            setIcon(
                R.id.analog_clock,
                "setSecondHand",
                Icon.createWithResource(context, options.secondHand)
            )
        }
    }
}