package com.bnyro.clock.presentation.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.presentation.widgets.DigitalClockWidgetConfig.Companion.InitialTextSize
import com.bnyro.clock.presentation.widgets.DigitalClockWidgetConfig.Companion.PREF_FILE
import com.bnyro.clock.ui.MainActivity

class DigitalClockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        appWidgetIds.forEach { appWidgetId ->
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
            val showDate = sharedPreferences.getBoolean(
                DigitalClockWidgetConfig.PREF_SHOW_DATE + appWidgetId,
                true
            )
            val textSize = sharedPreferences.getFloat(
                DigitalClockWidgetConfig.PREF_DATE_TEXT_SIZE + appWidgetId,
                InitialTextSize
            )
            views.setTextViewTextSize(R.id.textClock, TypedValue.COMPLEX_UNIT_SP, textSize)

            val visibility = if (showDate) View.VISIBLE else View.GONE
            views.setViewVisibility(R.id.textClock, visibility)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
