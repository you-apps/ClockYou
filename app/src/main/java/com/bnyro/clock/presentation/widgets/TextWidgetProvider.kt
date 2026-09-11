package com.bnyro.clock.presentation.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.bnyro.clock.util.widgets.deleteClockWidgetPref

abstract class TextWidgetProvider: AppWidgetProvider() {
    @get:LayoutRes
    abstract val widgetLayoutResource: Int

    abstract fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews)

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
            val viewMapping = RemoteViews(context.packageName, widgetLayoutResource)

            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RemoteViews(viewMapping)
            } else {
                viewMapping
            }

            applyClockWidgetOptions(context, appWidgetId, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.deleteClockWidgetPref(appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }
}