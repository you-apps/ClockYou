package com.bnyro.clock.util.widgets

import android.content.Context
import com.google.android.material.color.MaterialColors

enum class TextColor(val attrInt: Int, val directColor: Int? = null) {
    Primary(android.R.attr.colorPrimary),
    PrimaryDark(android.R.attr.colorPrimaryDark),
    Secondary(com.google.android.material.R.attr.colorSecondary),
    SecondaryVariant(com.google.android.material.R.attr.colorSecondaryVariant),
    Tertiary(com.google.android.material.R.attr.colorTertiary),
    White(android.R.color.white, android.graphics.Color.WHITE),
    Black(android.R.color.black, android.graphics.Color.BLACK);

    constructor(attrInt: Int) : this(attrInt, null)
}

fun TextColor.getColorValue(context: Context): Int {
    if (this.directColor != null) {
        return this.directColor
    }
    return MaterialColors.getColor(context, this.attrInt, 0)
}