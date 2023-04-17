package com.bnyro.clock.obj

import com.bnyro.clock.extensions.addZero

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
}
