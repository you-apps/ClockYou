package com.bnyro.clock.presentation.widgets

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.util.widgets.loadClockWidgetSettings

class DigitalClockWidget : TextWidgetProvider() {
    override val widgetLayoutResource = R.layout.digital_clock

    override fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews) {
        val options = context.loadClockWidgetSettings(appWidgetId, DefaultConfig)
        views.applyDigitalClockWidgetOptions(options)
    }

    companion object {
        val DefaultConfig = ClockWidgetOptions(
            dateTextSize = 16f,
            timeTextSize = 52f
        )

        fun RemoteViews.applyDigitalClockWidgetOptions(
            options: ClockWidgetOptions
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
    }
}
