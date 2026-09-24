package com.bnyro.clock.domain.model

data class AlarmFilters(
    val label: String = "",
    val weekDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val startTime: Long = 0, //00:00
    val endTime: Long = 86340000 //23:59
)
