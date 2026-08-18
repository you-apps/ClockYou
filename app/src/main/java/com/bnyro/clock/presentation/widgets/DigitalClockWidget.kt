package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
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

class DigitalClockWidget : TextWidgetProvider() {
    override val widgetLayoutResource = R.layout.digital_clock

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, this::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews) {
        val options = context.loadClockWidgetSettings(appWidgetId, DefaultConfig)
        views.applyDigitalClockWidgetOptions(context, options)
    }

    companion object {
        val DefaultConfig = ClockWidgetOptions(
            dateTextSize = 16f,
            timeTextSize = 52f,
            shadowPreset = ShadowPreset.OFF,
            openAppOnClick = true
        )

        fun RemoteViews.applyDigitalClockWidgetOptions(
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
                ShadowPreset.SUBTLE -> R.id.textClock_shadow_subtle
                ShadowPreset.SOFT   -> R.id.textClock_shadow_soft
                ShadowPreset.FLOAT  -> R.id.textClock_shadow_float
                ShadowPreset.DEEP   -> R.id.textClock_shadow_deep
                ShadowPreset.STRONG -> R.id.textClock_shadow_strong
                else                -> R.id.textClock
            }
            val timeId = when (effectivePreset) {
                ShadowPreset.SUBTLE -> R.id.textClock2_shadow_subtle
                ShadowPreset.SOFT   -> R.id.textClock2_shadow_soft
                ShadowPreset.FLOAT  -> R.id.textClock2_shadow_float
                ShadowPreset.DEEP   -> R.id.textClock2_shadow_deep
                ShadowPreset.STRONG -> R.id.textClock2_shadow_strong
                else                -> R.id.textClock2
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
            setViewVisibility(timeId, timeVisibility)
            setViewVisibility(cityId, timeZoneVisibility)

            setTextViewTextSize(dateId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize)
            setTextViewTextSize(cityId, TypedValue.COMPLEX_UNIT_SP, options.dateTextSize - 4)
            setTextViewTextSize(timeId, TypedValue.COMPLEX_UNIT_SP, options.timeTextSize)

            setString(dateId, "setTimeZone", options.timeZone)
            setString(timeId, "setTimeZone", options.timeZone)
            setTextViewText(cityId, options.timeZoneName)

            val timeColor = options.timeColor.getColorValue(context, options.customTimeColor)
            val dateColor = options.dateColor.getColorValue(context, options.customDateColor)
            setTextColor(dateId, dateColor)
            setTextColor(cityId, dateColor)
            setTextColor(timeId, timeColor)

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