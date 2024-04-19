package com.bnyro.clock.util.widgets

import android.content.Context

internal val PREF_FILE = "WidgetConfig"

internal val Context.widgetPreferences
    get() = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

// Digital Clock widget
internal const val PREF_SHOW_DATE = "showDate:"
internal const val PREF_SHOW_TIME = "showTime:"
internal const val PREF_SHOW_BACKGROUND = "showBackground:"
internal const val PREF_DATE_TEXT_SIZE = "dateTextSize:"
internal const val PREF_TIME_TEXT_SIZE = "timeTextSize:"
internal const val PREF_TIME_ZONE = "timeZone:"
internal const val PREF_TIME_ZONE_NAME = "timeZoneName:"