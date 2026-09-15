package com.bnyro.clock.util.widgets

import android.content.Context
import androidx.core.content.edit
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.domain.model.ShadowPreset

fun Context.saveClockWidgetSettings(
    appWidgetId: Int,
    options: ClockWidgetOptions
) {
    unignoreWidgetId(appWidgetId)
    widgetPreferences.edit {
        putBoolean(PREF_SHOW_DATE + appWidgetId, options.showDate)
        putBoolean(PREF_SHOW_TIME + appWidgetId, options.showTime)
        putFloat(PREF_DATE_TEXT_SIZE + appWidgetId, options.dateTextSize)
        putFloat(PREF_TIME_TEXT_SIZE + appWidgetId, options.timeTextSize)
        putString(PREF_TIME_ZONE + appWidgetId, options.timeZone)
        putString(PREF_TIME_ZONE_NAME + appWidgetId, options.timeZoneName)
        putBoolean(PREF_SHOW_BACKGROUND + appWidgetId, options.showBackground)
        putInt(PREF_DATE_TEXT_COLOR + appWidgetId, options.dateColor.attrInt)
        putInt(PREF_TIME_TEXT_COLOR + appWidgetId, options.timeColor.attrInt)
        if (options.customDateColor != null) {
            putInt(PREF_CUSTOM_DATE_COLOR + appWidgetId, options.customDateColor!!)
        } else {
            remove(PREF_CUSTOM_DATE_COLOR + appWidgetId)
        }
        if (options.customTimeColor != null) {
            putInt(PREF_CUSTOM_TIME_COLOR + appWidgetId, options.customTimeColor!!)
        } else {
            remove(PREF_CUSTOM_TIME_COLOR + appWidgetId)
        }
        putBoolean(PREF_OPEN_APP_ON_CLICK + appWidgetId, options.openAppOnClick)
        putString(PREF_SHADOW_PRESET + appWidgetId, options.shadowPreset.name)
        putFloat(PREF_SHADOW_RADIUS + appWidgetId, options.shadowRadius)
        putFloat(PREF_SHADOW_DX + appWidgetId, options.shadowDx)
        putFloat(PREF_SHADOW_DY + appWidgetId, options.shadowDy)
        putFloat(PREF_SHADOW_ALPHA + appWidgetId, options.shadowAlpha)
    }
}

fun Context.loadClockWidgetSettings(
    appWidgetId: Int, defaultClockWidgetOptions: ClockWidgetOptions
): ClockWidgetOptions = with(widgetPreferences) {
    val showDate = getBoolean(
        PREF_SHOW_DATE + appWidgetId,
        defaultClockWidgetOptions.showDate
    )
    val showTime = getBoolean(
        PREF_SHOW_TIME + appWidgetId,
        defaultClockWidgetOptions.showTime
    )

    val dateTextSize = getFloat(
        PREF_DATE_TEXT_SIZE + appWidgetId,
        defaultClockWidgetOptions.dateTextSize
    )

    val timeTextSize = getFloat(
        PREF_TIME_TEXT_SIZE + appWidgetId,
        defaultClockWidgetOptions.timeTextSize
    )

    val timeZone = getString(
        PREF_TIME_ZONE + appWidgetId,
        defaultClockWidgetOptions.timeZone
    )

    val timeZoneName = getString(
        PREF_TIME_ZONE_NAME + appWidgetId,
        defaultClockWidgetOptions.timeZoneName
    ) ?: defaultClockWidgetOptions.timeZoneName
    val showBackground = getBoolean(
        PREF_SHOW_BACKGROUND + appWidgetId,
        defaultClockWidgetOptions.showBackground
    )

    val dateColor = getInt(
        PREF_DATE_TEXT_COLOR + appWidgetId,
        defaultClockWidgetOptions.dateColor.attrInt
    ).let { attrInt ->
        ClockWidgetOptions.textColorOptions.find { it.attrInt == attrInt }
            ?: when (attrInt) {
                android.R.attr.colorPrimary -> TextColor.Primary
                android.R.attr.colorPrimaryDark -> TextColor.PrimaryDark
                com.google.android.material.R.attr.colorSecondary -> TextColor.Secondary
                com.google.android.material.R.attr.colorSecondaryVariant -> TextColor.SecondaryVariant
                com.google.android.material.R.attr.colorTertiary -> TextColor.Tertiary
                android.R.color.white -> TextColor.White
                android.R.color.black -> TextColor.Black
                else -> defaultClockWidgetOptions.dateColor
            }
    }

    val timeColor = getInt(
        PREF_TIME_TEXT_COLOR + appWidgetId,
        defaultClockWidgetOptions.timeColor.attrInt
    ).let { attrInt ->
        ClockWidgetOptions.textColorOptions.find { it.attrInt == attrInt }
            ?: when (attrInt) {
                android.R.attr.colorPrimary -> TextColor.Primary
                android.R.attr.colorPrimaryDark -> TextColor.PrimaryDark
                com.google.android.material.R.attr.colorSecondary -> TextColor.Secondary
                com.google.android.material.R.attr.colorSecondaryVariant -> TextColor.SecondaryVariant
                com.google.android.material.R.attr.colorTertiary -> TextColor.Tertiary
                android.R.color.white -> TextColor.White
                android.R.color.black -> TextColor.Black
                else -> defaultClockWidgetOptions.timeColor
            }
    }


    // Migration: if the new key is absent but the legacy boolean exists, promote it once.
    val shadowPreset = if (contains(PREF_SHADOW_PRESET + appWidgetId)) {
        val name = getString(PREF_SHADOW_PRESET + appWidgetId, ShadowPreset.OFF.name) ?: ShadowPreset.OFF.name
        runCatching { ShadowPreset.valueOf(name) }.getOrDefault(ShadowPreset.OFF)
    } else if (contains(PREF_USE_SHADOW_LAYOUT + appWidgetId)) {
        val legacy = getBoolean(PREF_USE_SHADOW_LAYOUT + appWidgetId, false)
        if (legacy) ShadowPreset.STRONG else ShadowPreset.OFF
    } else {
        defaultClockWidgetOptions.shadowPreset
    }

    val shadowRadius = getFloat(
        PREF_SHADOW_RADIUS + appWidgetId,
        defaultClockWidgetOptions.shadowRadius
    )
    val shadowDx = getFloat(
        PREF_SHADOW_DX + appWidgetId,
        defaultClockWidgetOptions.shadowDx
    )
    val shadowDy = getFloat(
        PREF_SHADOW_DY + appWidgetId,
        defaultClockWidgetOptions.shadowDy
    )
    val shadowAlpha = getFloat(
        PREF_SHADOW_ALPHA + appWidgetId,
        defaultClockWidgetOptions.shadowAlpha
    )

    val openAppOnClick = getBoolean(
        PREF_OPEN_APP_ON_CLICK + appWidgetId,
        defaultClockWidgetOptions.openAppOnClick
    )

    val customDateColor = if (contains(PREF_CUSTOM_DATE_COLOR + appWidgetId)) {
        getInt(PREF_CUSTOM_DATE_COLOR + appWidgetId, 0)
    } else {
        defaultClockWidgetOptions.customDateColor
    }

    val customTimeColor = if (contains(PREF_CUSTOM_TIME_COLOR + appWidgetId)) {
        getInt(PREF_CUSTOM_TIME_COLOR + appWidgetId, 0)
    } else {
        defaultClockWidgetOptions.customTimeColor
    }

    return ClockWidgetOptions(
        showDate = showDate,
        showTime = showTime,
        dateTextSize = dateTextSize,
        timeTextSize = timeTextSize,
        dateColor = dateColor,
        timeColor = timeColor,
        timeZone = timeZone,
        timeZoneName = timeZoneName,
        showBackground = showBackground,
        shadowPreset = shadowPreset,
        shadowRadius = shadowRadius,
        shadowDx = shadowDx,
        shadowDy = shadowDy,
        shadowAlpha = shadowAlpha,
        openAppOnClick = openAppOnClick,
        customTimeColor = customTimeColor,
        customDateColor = customDateColor
    )
}

fun Context.deleteClockWidgetPref(appWidgetId: Int) =
    widgetPreferences.edit {
        remove(PREF_SHOW_DATE + appWidgetId)
        remove(PREF_SHOW_TIME + appWidgetId)
        remove(PREF_SHOW_BACKGROUND + appWidgetId)
        remove(PREF_DATE_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_TEXT_SIZE + appWidgetId)
        remove(PREF_TIME_ZONE + appWidgetId)
        remove(PREF_TIME_ZONE_NAME + appWidgetId)
        remove(PREF_DATE_TEXT_COLOR + appWidgetId)
        remove(PREF_TIME_TEXT_COLOR + appWidgetId)
        remove(PREF_CUSTOM_DATE_COLOR + appWidgetId)
        remove(PREF_CUSTOM_TIME_COLOR + appWidgetId)
        remove(PREF_USE_SHADOW_LAYOUT + appWidgetId) // legacy
        remove(PREF_SHADOW_PRESET + appWidgetId)
        remove(PREF_SHADOW_RADIUS + appWidgetId)
        remove(PREF_SHADOW_DX + appWidgetId)
        remove(PREF_SHADOW_DY + appWidgetId)
        remove(PREF_SHADOW_ALPHA + appWidgetId)
    }