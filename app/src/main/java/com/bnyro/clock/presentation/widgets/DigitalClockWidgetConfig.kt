package com.bnyro.clock.presentation.widgets

import android.content.Context
import android.widget.RemoteViews
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.presentation.widgets.DigitalClockWidget.Companion.applyDigitalClockWidgetOptions

class DigitalClockWidgetConfig: ClockWidgetConfig() {
    override val defaultOptions: ClockWidgetOptions = DigitalClockWidget.DefaultConfig
    override val widgetLayoutResource: Int = R.layout.digital_clock

    override fun updateClockWidget(context: Context, views: RemoteViews, options: ClockWidgetOptions) {
        views.applyDigitalClockWidgetOptions(context, options)
    }
}