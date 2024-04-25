package com.bnyro.clock.domain.model

import androidx.annotation.DrawableRes
import com.bnyro.clock.R

sealed class AnalogClockFace(
    val name: String,
    val author: String? = null,
    val authorUrl: String? = null,
    @DrawableRes val dial: Int = 0,
    @DrawableRes val hourHand: Int = 0,
    @DrawableRes val minuteHand: Int = 0,
    @DrawableRes val secondHand: Int = 0
) {

    object System : AnalogClockFace(
        name = "System"
    )

    object Classic76 : AnalogClockFace(
        name = "Classic76",
        author = "§",
        authorUrl = "https://github.com/shuvashish76",
        dial = R.drawable.classic76_dial,
        hourHand = R.drawable.classic76_hour_hand,
        minuteHand = R.drawable.classic76_minute_hand,
        secondHand = R.drawable.classic76_second_hand
    )

    object TwoDBall : AnalogClockFace(
        name = "2D Ball",
        author = "§",
        authorUrl = "https://github.com/shuvashish76",
        dial = R.drawable.twod_ball_dial,
        hourHand = R.drawable.twod_ball_hour_hand,
        minuteHand = R.drawable.twod_ball_minute_hand,
        secondHand = R.drawable.twod_ball_second_hand
    )

    object ClassicClockYou : AnalogClockFace(
        name = "Classic Clock You",
        author = "SuhasDissa",
        authorUrl = "https://github.com/SuhasDissa",
        dial = R.drawable.classic_clock_you_dial,
        hourHand = R.drawable.classic_clock_you_hour_hand,
        minuteHand = R.drawable.classic_clock_you_minute_hand
    )

    object AnalogClock : AnalogClockFace(
        name = "Analog Clock",
        author = "DG-RA",
        authorUrl = "https://openclipart.org/artist/DG-RA",
        dial = R.drawable.analog_clock_black_dial,
        hourHand = R.drawable.analog_clock_black_hour_hand,
        minuteHand = R.drawable.analog_clock_black_minute_hand,
        secondHand = R.drawable.analog_clock_black_second_hand
    )

    object MinimalisticCircular : AnalogClockFace(
        name = "Minimalistic - Circular",
        author = "§",
        authorUrl = "https://github.com/shuvashish76",
        dial = R.drawable.minimalistic_circular_dial,
        hourHand = R.drawable.minimalistic_circular_hour_hand,
        minuteHand = R.drawable.minimalistic_circular_minute_hand
    )

    object MinimalisticBar : AnalogClockFace(
        name = "Minimalistic - Bar",
        author = "§",
        authorUrl = "https://github.com/shuvashish76",
        dial = R.drawable.minimalistic_bar_dial,
        hourHand = R.drawable.minimalistic_bar_hour_hand,
        minuteHand = R.drawable.minimalistic_bar_minute_hand
    )

    companion object {
        val all = listOf(
            System,
            Classic76,
            TwoDBall,
            ClassicClockYou,
            AnalogClock,
            MinimalisticCircular,
            MinimalisticBar
        )
    }

}
