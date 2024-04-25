package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.widgets.applyAnalogClockWidgetOptions
import com.bnyro.clock.util.widgets.deleteAnalogClockWidgetPref
import com.bnyro.clock.util.widgets.loadAnalogClockWidgetSettings

class AnalogClockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.analog_clock).apply {
                setOnClickPendingIntent(R.id.analog_clock, pendingIntent)
            }
            val options = context.loadAnalogClockWidgetSettings(appWidgetId)
            views.applyAnalogClockWidgetOptions(options, context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.deleteAnalogClockWidgetPref(appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }
}
