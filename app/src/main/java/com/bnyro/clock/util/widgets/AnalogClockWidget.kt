package com.bnyro.clock.util.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
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
        remove(PREF_CLOCK_FACE_NAME + appWidgetId)
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
    removeAllViews(R.id.analog_clock_container)
    val freshClockView = RemoteViews(context.packageName, R.layout.analog_clock_view)
    addView(R.id.analog_clock_container, freshClockView)
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    setOnClickPendingIntent(R.id.analog_clock_container, pendingIntent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (options.dial != 0) {
            context.getBitmapFromDrawable(options.dial)?.let {
                setIcon(R.id.analog_clock, "setDial", Icon.createWithBitmap(it))
            }
        }

        if (options.hourHand != 0) {
            context.getBitmapFromDrawable(options.hourHand)?.let {
                setIcon(R.id.analog_clock, "setHourHand", Icon.createWithBitmap(it))
            }
        }

        if (options.minuteHand != 0) {
            context.getBitmapFromDrawable(options.minuteHand)?.let {
                setIcon(R.id.analog_clock, "setMinuteHand", Icon.createWithBitmap(it))
            }
        }

        if (options.secondHand != 0) {
            context.getBitmapFromDrawable(options.secondHand)?.let {
                setIcon(R.id.analog_clock, "setSecondHand", Icon.createWithBitmap(it))
            }
        }
    }
}
private fun Context.getBitmapFromDrawable(drawableId: Int): Bitmap? {
    if (drawableId == 0) return null
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 200
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 200

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}