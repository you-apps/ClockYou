package com.bnyro.clock.domain.model

import androidx.annotation.DrawableRes

data class AnalogClockWidgetOptions(
    var clockFaceName: String,
    @DrawableRes var hourHand: Int,
    @DrawableRes var minuteHand: Int,
    @DrawableRes var secondHand: Int,
    @DrawableRes var dial: Int
)