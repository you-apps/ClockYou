package com.bnyro.clock.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SizeF
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.ui.MainActivity

class VerticalClockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(150f, 100f) to RemoteViews(context.packageName, R.layout.vertical_clock_small).apply {
                    setOnClickPendingIntent(R.id.container, pendingIntent)
                },
                SizeF(215f, 100f) to RemoteViews(context.packageName, R.layout.vertical_clock_large).apply {
                    setOnClickPendingIntent(R.id.container, pendingIntent)
                }
            )

            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RemoteViews(viewMapping)
            } else {
                viewMapping.values.first()
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
