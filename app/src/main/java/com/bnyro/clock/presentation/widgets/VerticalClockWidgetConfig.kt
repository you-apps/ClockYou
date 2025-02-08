package com.bnyro.clock.presentation.widgets

import android.content.Context
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.presentation.widgets.VerticalClockWidget.Companion.applyVerticalClockWidgetOptions

class VerticalClockWidgetConfig: ClockWidgetConfig() {
    override val defaultOptions: ClockWidgetOptions = VerticalClockWidget.DefaultConfig
    override val widgetLayoutResource: Int = R.layout.vertical_clock

    override fun updateClockWidget(context: Context, views: RemoteViews, options: ClockWidgetOptions) {
        views.applyVerticalClockWidgetOptions(context, options)
    }
}