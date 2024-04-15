package com.bnyro.clock.domain.model

import com.bnyro.clock.util.extensions.addZero

data class TimeObject(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val milliseconds: Int = 0
) {
    override fun toString(): String {
        return "${hours.addZero()}:${minutes.addZero()}:${seconds.addZero()}.${(milliseconds / 10).addZero()}"
            .replace("^(00:)*".toRegex(), "")
    }

    fun toFullString(): String {
        return String.format("%02d:%02d.%02d", minutes + hours * 60, seconds, milliseconds / 10)
    }

    operator fun minus(value: TimeObject): TimeObject {
        var hours =
            (this.hours - value.hours)
        var minutes =
            (this.minutes - value.minutes).also { if (it < 0) hours -= 1 }
                .let { if (it < 0) 60 + it else it }
        var seconds =
            (this.seconds - value.seconds).also { if (it < 0) minutes -= 1 }
                .let { if (it < 0) 60 + it else it }
        val milliseconds =
            (this.milliseconds - value.milliseconds).also { if (it < 0) seconds -= 1 }
                .let { if (it < 0) 1000 + it else it }

        return TimeObject(
            hours,
            minutes,
            seconds,
            milliseconds
        )
    }
}
