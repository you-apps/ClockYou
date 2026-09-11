package com.bnyro.clock.presentation.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.util.widgets.applyAnalogClockWidgetOptions
import com.bnyro.clock.util.widgets.deleteAnalogClockWidgetPref
import com.bnyro.clock.util.widgets.loadAnalogClockWidgetSettings

class AnalogClockWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, this::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.analog_clock)
            val options = context.loadAnalogClockWidgetSettings(appWidgetId)
            views.applyAnalogClockWidgetOptions(appWidgetId, options, context)
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
