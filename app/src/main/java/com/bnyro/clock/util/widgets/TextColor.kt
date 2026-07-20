package com.bnyro.clock.util.widgets

import android.content.Context
import com.google.android.material.color.MaterialColors

enum class TextColor(val attrInt: Int) {
    Primary(android.R.attr.colorPrimary),
    PrimaryDark(android.R.attr.colorPrimaryDark),
    Secondary(com.google.android.material.R.attr.colorSecondary),
    SecondaryVariant(com.google.android.material.R.attr.colorSecondaryVariant),
    Tertiary(com.google.android.material.R.attr.colorTertiary)
}

fun TextColor.getColorValue(context: Context): Int {
    return MaterialColors.getColor(context, this.attrInt, -1)
}