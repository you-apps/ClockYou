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
            timeTextSize = 80f,
            useShadowLayout = false
        )

        fun RemoteViews.applyVerticalClockWidgetOptions(
            context: Context,
            options: ClockWidgetOptions
        ) {

            val normalVisibility = if (options.useShadowLayout) View.GONE else View.VISIBLE
            val shadowVisibility = if (options.useShadowLayout) View.VISIBLE else View.GONE

            setViewVisibility(R.id.container_normal, normalVisibility)
            setViewVisibility(R.id.container_shadow, shadowVisibility)

            val dateId = if (options.useShadowLayout) R.id.textClockDate_shadow else R.id.textClockDate
            val hoursId = if (options.useShadowLayout) R.id.textClockHours_shadow else R.id.textClockHours
            val minutesId = if (options.useShadowLayout) R.id.textClockMinutes_shadow else R.id.textClockMinutes
            val cityId = if (options.useShadowLayout) R.id.cityName_shadow else R.id.cityName

            val dateVisibility = if (options.showDate) View.VISIBLE else View.GONE
            val timeVisibility = if (options.showTime) View.VISIBLE else View.GONE
            val timeZoneVisibility = if (options.timeZone == null) View.GONE else View.VISIBLE
            val backgroundResource = if (options.showBackground) R.drawable.widget_shape else 0

            setViewVisibility(dateId, dateVisibility)
            setViewVisibility(hoursId, timeVisibility)
            setViewVisibility(minutesId, timeVisibility)
            setViewVisibility(cityId, timeZoneVisibility)

            setTextViewTextSize(dateId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)
            setTextViewTextSize(hoursId, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)
            setTextViewTextSize(minutesId, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)
            setTextViewTextSize(cityId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)

            setString(hoursId, "setTimeZone", options.timeZone)
            setString(minutesId, "setTimeZone", options.timeZone)
            setString(dateId, "setTimeZone", options.timeZone)
            setTextViewText(cityId, options.timeZoneName)


            val timeColor = options.timeColor.getColorValue(context)
            val dateColor = options.dateColor.getColorValue(context)
            if (timeColor != -1 && dateColor != -1) {
                setTextColor(hoursId, timeColor)
                setTextColor(minutesId, timeColor)
                setTextColor(dateId, dateColor)
                setTextColor(cityId, dateColor)
            }

            setInt(R.id.frameLayout, "setBackgroundResource", backgroundResource)
        }
    }
}