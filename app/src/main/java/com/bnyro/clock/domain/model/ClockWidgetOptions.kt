package com.bnyro.clock.domain.model

import com.bnyro.clock.util.widgets.TextColor

data class ClockWidgetOptions(
    var showDate: Boolean = true,
    var showTime: Boolean = true,
    var timeZone: String? = null,
    var timeZoneName: String = "",
    var showBackground: Boolean = true,
    var dateTextSize: Float,
    var timeTextSize: Float,
    var timeColor: TextColor = TextColor.Primary,
    var dateColor: TextColor = TextColor.Secondary
) {
    companion object {
        val dateSizeOptions = listOf(
            12f,
            16f,
            20f,
            24f,
            28f,
            32f
        )

        val timeSizeOptions = listOf(
            36f,
            40f,
            44f,
            48f,
            52f,
            56f,
            60f,
            64f,
            68f,
            72f,
            76f,
            80f,
            84f,
            88f,
            92f,
            96f,
            100f
        )

        val textColorOptions = TextColor.entries.toList()
    }
}