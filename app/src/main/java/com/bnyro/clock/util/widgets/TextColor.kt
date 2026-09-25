package com.bnyro.clock.util.widgets

import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import com.bnyro.clock.R

enum class TextColor(val attrInt: Int, @param:ColorRes val colorRes: Int?) {
    Primary(android.R.attr.colorPrimary, R.color.widget_color_primary),
    PrimaryDark(android.R.attr.colorPrimaryDark, R.color.widget_color_primary_dark),
    Secondary(com.google.android.material.R.attr.colorSecondary, R.color.widget_color_secondary),
    SecondaryVariant(com.google.android.material.R.attr.colorSecondaryVariant, R.color.widget_color_secondary_variant),
    Tertiary(com.google.android.material.R.attr.colorTertiary, R.color.widget_color_tertiary),
    White(android.R.color.white, android.R.color.white),
    Black(android.R.color.black, android.R.color.black),
    Custom(0, null)
}

fun TextColor.getColorValue(context: Context, customColorInt: Int? = null): Int {
    return if (colorRes != null) {
        context.getColor(colorRes)
    } else {
        customColorInt ?: context.getColor(android.R.color.white)
    }
}

fun RemoteViews.applyTextColor(
    context: Context,
    viewId: Int,
    textColor: TextColor,
    customColorInt: Int? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && textColor.colorRes != null) {
        setColor(viewId, "setTextColor", textColor.colorRes)
    } else {
        setTextColor(viewId, textColor.getColorValue(context, customColorInt))
    }
}
