package com.bnyro.clock.extensions

fun Int.addZero(): String {
    if (this >= 10) return this.toString()
    return "0$this"
}
