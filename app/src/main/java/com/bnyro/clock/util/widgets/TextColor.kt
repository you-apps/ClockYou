package com.bnyro.clock.util.widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
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
    Black(android.R.color.black, Color.BLACK),
    Custom(0, null);

    constructor(attrInt: Int) : this(attrInt, null)
}

fun TextColor.getColorValue(context: Context, customColorInt: Int? = null): Int {
    if (this == TextColor.Custom) {
        return customColorInt ?: Color.WHITE
    }

    if (this.directColor != null) {
        return this.directColor
    }

    val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val systemColorRes = if (isNight) {
            when (this) {
                TextColor.Primary -> android.R.color.system_accent1_200
                TextColor.PrimaryDark -> android.R.color.system_accent1_100
                TextColor.Secondary -> android.R.color.system_accent2_200
                TextColor.SecondaryVariant -> android.R.color.system_accent2_100
                TextColor.Tertiary -> android.R.color.system_accent3_200
                else -> null
            }
        } else {
            when (this) {
                TextColor.Primary -> android.R.color.system_accent1_600
                TextColor.PrimaryDark -> android.R.color.system_accent1_900
                TextColor.Secondary -> android.R.color.system_accent2_600
                TextColor.SecondaryVariant -> android.R.color.system_accent2_700
                TextColor.Tertiary -> android.R.color.system_accent3_600
                else -> null
            }
        }
        if (systemColorRes != null) {
            return context.getColor(systemColorRes)
        }
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
        TextColor.Secondary -> if (isNight) Color.LTGRAY else Color.DKGRAY
        TextColor.SecondaryVariant -> if (isNight) Color.DKGRAY else Color.LTGRAY
        else -> if (isNight) Color.WHITE else Color.BLACK
    }
}

fun TextColor.getColorRes(): Int? = when (this) {
    TextColor.Primary -> R.color.widget_color_primary
    TextColor.PrimaryDark -> R.color.widget_color_primary_dark
    TextColor.Secondary -> R.color.widget_color_secondary
    TextColor.SecondaryVariant -> R.color.widget_color_secondary_variant
    TextColor.Tertiary -> R.color.widget_color_tertiary
    TextColor.White -> android.R.color.white
    TextColor.Black -> android.R.color.black
    TextColor.Custom -> null
}

fun android.widget.RemoteViews.applyTextColor(
    context: Context,
    viewId: Int,
    textColor: TextColor,
    customColorInt: Int? = null
) {
    if (textColor == TextColor.Custom) {
        setTextColor(viewId, customColorInt ?: Color.WHITE)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val colorRes = textColor.getColorRes()
        if (colorRes != null) {
            setColor(viewId, "setTextColor", colorRes)
        } else {
            setTextColor(viewId, textColor.getColorValue(context, customColorInt))
        }
    } else {
        setTextColor(viewId, textColor.getColorValue(context, customColorInt))
    }
}