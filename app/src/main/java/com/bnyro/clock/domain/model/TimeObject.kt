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
        val total = ((hours - value.hours).toLong() * 3600 +
            (minutes - value.minutes) * 60 + seconds - value.seconds) * 1000 +
            milliseconds - value.milliseconds

        return TimeObject(
            hours = (total / 3_600_000).toInt(),
            minutes = (total / 60_000 % 60).toInt(),
            seconds = (total / 1000 % 60).toInt(),
            milliseconds = (total % 1000).toInt()
        )
    }
}
