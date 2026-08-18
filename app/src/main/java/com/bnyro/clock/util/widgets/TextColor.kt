package com.bnyro.clock.util.widgets

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.bnyro.clock.R
import com.google.android.material.color.MaterialColors

enum class TextColor(val attrInt: Int, val directColor: Int? = null) {
    Primary(android.R.attr.colorPrimary),
    PrimaryDark(android.R.attr.colorPrimaryDark),
    Secondary(com.google.android.material.R.attr.colorSecondary),
    SecondaryVariant(com.google.android.material.R.attr.colorSecondaryVariant),
    Tertiary(com.google.android.material.R.attr.colorTertiary),
    White(android.R.color.white, Color.WHITE),
    Black(android.R.color.black, Color.BLACK);

    constructor(attrInt: Int) : this(attrInt, null)
}

fun TextColor.getColorValue(context: Context): Int {
    if (this.directColor != null) {
        return this.directColor
    }
    val themedContext = ContextThemeWrapper(context, R.style.Theme_ClockYou)
    val typedValue = TypedValue()
    if (themedContext.theme.resolveAttribute(this.attrInt, typedValue, true)) {
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
            typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return typedValue.data
        }
        if (typedValue.resourceId != 0) {
            runCatching {
                return ContextCompat.getColor(themedContext, typedValue.resourceId)
            }
        }
    }
    val matColor = MaterialColors.getColor(themedContext, this.attrInt, 0)
    if (matColor != 0) {
        return matColor
    }
    return when (this) {
        TextColor.Black -> Color.BLACK
        TextColor.Secondary -> Color.LTGRAY
        TextColor.SecondaryVariant -> Color.DKGRAY
        else -> Color.WHITE
    }
}