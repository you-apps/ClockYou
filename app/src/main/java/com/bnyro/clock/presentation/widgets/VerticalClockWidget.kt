package com.bnyro.clock.presentation.widgets

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.util.widgets.getColorValue
import com.bnyro.clock.util.widgets.loadClockWidgetSettings

class VerticalClockWidget : TextWidgetProvider() {
    override val widgetLayoutResource = R.layout.vertical_clock

    override fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews) {
        val options = context.loadClockWidgetSettings(appWidgetId, DefaultConfig)
        views.applyVerticalClockWidgetOptions(context, options)
    }

    companion object {
        val DefaultConfig = ClockWidgetOptions(
            dateTextSize = 10f,
            timeTextSize = 80f
        )

        fun RemoteViews.applyVerticalClockWidgetOptions(
            context: Context,
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

            setViewVisibility(R.id.textClockDate, dateVisibility)
            setViewVisibility(R.id.textClockHours, timeVisibility)
            setViewVisibility(R.id.textClockMinutes, timeVisibility)

            setTextViewTextSize(R.id.textClockDate, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)
            setTextViewTextSize(R.id.textClockHours, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)
            setTextViewTextSize(R.id.textClockMinutes, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)
            setTextViewTextSize(R.id.cityName, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)

            setString(R.id.textClockHours, "setTimeZone", options.timeZone)
            setString(R.id.textClockMinutes, "setTimeZone", options.timeZone)
            setString(R.id.textClockDate, "setTimeZone", options.timeZone)

            val timeColor = options.timeColor.getColorValue(context)
            val dateColor = options.dateColor.getColorValue(context)
            if (timeColor != -1 && dateColor != -1) {
                setTextColor(R.id.textClockHours, timeColor)
                setTextColor(R.id.textClockMinutes, timeColor)
                setTextColor(R.id.textClockDate, dateColor)
                setTextColor(R.id.cityName, dateColor)
            }

            setInt(R.id.frameLayout, "setBackgroundResource", backgroundResource)

            setViewVisibility(R.id.cityName, timeZoneVisibility)
            setTextViewText(R.id.cityName, options.timeZoneName)
        }
    }
}
