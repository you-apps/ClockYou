package com.bnyro.clock.util.widgets

import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import com.bnyro.clock.R
import com.google.android.material.color.MaterialColors

enum class TextColor(val attrInt: Int, val directColor: Int? = null) {
    Primary(android.R.attr.colorPrimary),
    PrimaryDark(android.R.attr.colorPrimaryDark),
    Secondary(com.google.android.material.R.attr.colorSecondary),
    SecondaryVariant(com.google.android.material.R.attr.colorSecondaryVariant),
    Tertiary(com.google.android.material.R.attr.colorTertiary),
    White(android.R.color.white, android.graphics.Color.WHITE),
    Black(android.R.color.black, android.graphics.Color.BLACK),
    Custom(0, null);

    constructor(attrInt: Int) : this(attrInt, null)
}

fun TextColor.getColorValue(context: Context, customColorInt: Int? = null): Int {
    if (this == TextColor.Custom && customColorInt != null) {
        return customColorInt
    }

    if (this.directColor != null) {
        return this.directColor
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val systemColorRes = when (this) {
            TextColor.Primary -> android.R.color.system_accent1_500
            TextColor.PrimaryDark -> android.R.color.system_accent1_900
            TextColor.Secondary -> android.R.color.system_accent2_500
            TextColor.SecondaryVariant -> android.R.color.system_accent2_700
            TextColor.Tertiary -> android.R.color.system_accent3_500
            else -> null
        }
        if (systemColorRes != null) {
            return context.getColor(systemColorRes)
        }
    }

    val themedContext = ContextThemeWrapper(context, R.style.Theme_ClockYou)
    return MaterialColors.getColor(themedContext, this.attrInt, 0)
}