package com.bnyro.clock.domain.model

data class VibrationPattern(
    val name: String,
    val pattern: List<Int>,
) {
    val length: Int = pattern.sum()
    val cumulative: List<Int> = pattern.scan(0, Int::plus).drop(1)
    val fractionalCumulative: List<Float> = cumulative.map { it.toFloat() / length }
}