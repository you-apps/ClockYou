package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.widgets.applyDigitalClockWidgetOptions
import com.bnyro.clock.util.widgets.deleteDigitalClockWidgetPref
import com.bnyro.clock.util.widgets.loadDigitalClockWidgetSettings

class DigitalClockWidget : AppWidgetProvider() {

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

            val viewMapping = RemoteViews(context.packageName, R.layout.digital_clock).apply {
                setOnClickPendingIntent(R.id.container, pendingIntent)
            }

            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RemoteViews(viewMapping)
            } else {
                viewMapping
            }
            val options = context.loadDigitalClockWidgetSettings(appWidgetId)
            views.applyDigitalClockWidgetOptions(options)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.deleteDigitalClockWidgetPref(appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }
}
