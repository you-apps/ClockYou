package com.bnyro.clock.presentation.widgets

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.util.widgets.getColorValue
import com.bnyro.clock.util.widgets.loadClockWidgetSettings

class DigitalClockWidget : TextWidgetProvider() {
    override val widgetLayoutResource = R.layout.digital_clock

    override fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews) {
        val options = context.loadClockWidgetSettings(appWidgetId, DefaultConfig)
        views.applyDigitalClockWidgetOptions(context, options)
    }

    companion object {
        val DefaultConfig = ClockWidgetOptions(
            dateTextSize = 16f,
            timeTextSize = 52f,
            useShadowLayout = false
        )

        fun RemoteViews.applyDigitalClockWidgetOptions(
            context: Context,
            options: ClockWidgetOptions
        ) {
            val normalVisibility = if (options.useShadowLayout) View.GONE else View.VISIBLE
            val shadowVisibility = if (options.useShadowLayout) View.VISIBLE else View.GONE

            setViewVisibility(R.id.container_normal, normalVisibility)
            setViewVisibility(R.id.container_shadow, shadowVisibility)

            val dateId = if (options.useShadowLayout) R.id.textClock_shadow else R.id.textClock
            val timeId = if (options.useShadowLayout) R.id.textClock2_shadow else R.id.textClock2
            val cityId = if (options.useShadowLayout) R.id.cityName_shadow else R.id.cityName

            val dateVisibility = if (options.showDate) View.VISIBLE else View.GONE
            val timeVisibility = if (options.showTime) View.VISIBLE else View.GONE
            val timeZoneVisibility = if (options.timeZone == null) View.GONE else View.VISIBLE
            val backgroundResource = if (options.showBackground) R.drawable.widget_shape else 0

            setViewVisibility(dateId, dateVisibility)
            setViewVisibility(timeId, timeVisibility)
            setViewVisibility(cityId, timeZoneVisibility)

            setTextViewTextSize(dateId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)
            setTextViewTextSize(cityId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize - 4)
            setTextViewTextSize(timeId, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)

            setString(dateId, "setTimeZone", options.timeZone)
            setString(timeId, "setTimeZone", options.timeZone)
            setTextViewText(cityId, options.timeZoneName)

            val timeColor = options.timeColor.getColorValue(context)
            val dateColor = options.dateColor.getColorValue(context)
            if (timeColor != -1 && dateColor != -1) {
                setTextColor(dateId, dateColor)
                setTextColor(cityId, dateColor)
                setTextColor(timeId, timeColor)
            }

            setInt(R.id.frameLayout, "setBackgroundResource", backgroundResource)
        }
    }
}