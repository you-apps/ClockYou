package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.bnyro.clock.R
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.widgets.deleteClockWidgetPref

abstract class TextWidgetProvider: AppWidgetProvider() {
    @get:LayoutRes
    abstract val widgetLayoutResource: Int

    abstract fun applyClockWidgetOptions(context: Context, appWidgetId: Int, views: RemoteViews)

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

            val viewMapping = RemoteViews(context.packageName, widgetLayoutResource).apply {
                setOnClickPendingIntent(R.id.container, pendingIntent)
            }

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