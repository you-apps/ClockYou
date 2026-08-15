package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.domain.model.ShadowPreset
import com.bnyro.clock.ui.MainActivity
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
            shadowPreset = ShadowPreset.OFF,
            openAppOnClick = true
        )

        fun RemoteViews.applyVerticalClockWidgetOptions(
            context: Context,
            options: ClockWidgetOptions
        ) {
            val effectivePreset = if (options.showBackground) ShadowPreset.OFF else options.shadowPreset

            setViewVisibility(R.id.container_normal,        if (effectivePreset == ShadowPreset.OFF)    View.VISIBLE else View.GONE)
            setViewVisibility(R.id.container_shadow_subtle, if (effectivePreset == ShadowPreset.SUBTLE) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.container_shadow_soft,   if (effectivePreset == ShadowPreset.SOFT)   View.VISIBLE else View.GONE)
            setViewVisibility(R.id.container_shadow_float,  if (effectivePreset == ShadowPreset.FLOAT)  View.VISIBLE else View.GONE)
            setViewVisibility(R.id.container_shadow_deep,   if (effectivePreset == ShadowPreset.DEEP)   View.VISIBLE else View.GONE)
            setViewVisibility(R.id.container_shadow_strong, if (effectivePreset == ShadowPreset.STRONG) View.VISIBLE else View.GONE)

            val dateId = when (effectivePreset) {
                ShadowPreset.SUBTLE -> R.id.textClockDate_shadow_subtle
                ShadowPreset.SOFT   -> R.id.textClockDate_shadow_soft
                ShadowPreset.FLOAT  -> R.id.textClockDate_shadow_float
                ShadowPreset.DEEP   -> R.id.textClockDate_shadow_deep
                ShadowPreset.STRONG -> R.id.textClockDate_shadow_strong
                else                -> R.id.textClockDate
            }
            val hoursId = when (effectivePreset) {
                ShadowPreset.SUBTLE -> R.id.textClockHours_shadow_subtle
                ShadowPreset.SOFT   -> R.id.textClockHours_shadow_soft
                ShadowPreset.FLOAT  -> R.id.textClockHours_shadow_float
                ShadowPreset.DEEP   -> R.id.textClockHours_shadow_deep
                ShadowPreset.STRONG -> R.id.textClockHours_shadow_strong
                else                -> R.id.textClockHours
            }
            val minutesId = when (effectivePreset) {
                ShadowPreset.SUBTLE -> R.id.textClockMinutes_shadow_subtle
                ShadowPreset.SOFT   -> R.id.textClockMinutes_shadow_soft
                ShadowPreset.FLOAT  -> R.id.textClockMinutes_shadow_float
                ShadowPreset.DEEP   -> R.id.textClockMinutes_shadow_deep
                ShadowPreset.STRONG -> R.id.textClockMinutes_shadow_strong
                else                -> R.id.textClockMinutes
            }
            val cityId = when (effectivePreset) {
                ShadowPreset.SUBTLE -> R.id.cityName_shadow_subtle
                ShadowPreset.SOFT   -> R.id.cityName_shadow_soft
                ShadowPreset.FLOAT  -> R.id.cityName_shadow_float
                ShadowPreset.DEEP   -> R.id.cityName_shadow_deep
                ShadowPreset.STRONG -> R.id.cityName_shadow_strong
                else                -> R.id.cityName
            }

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

            if (options.openAppOnClick) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.frameLayout, pendingIntent)
            } else {
                setOnClickPendingIntent(R.id.frameLayout, null)
            }
        }
    }
}