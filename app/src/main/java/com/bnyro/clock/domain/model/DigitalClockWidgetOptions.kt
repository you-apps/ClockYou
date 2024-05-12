package com.bnyro.clock.domain.model

data class DigitalClockWidgetOptions(
    var showDate: Boolean = true,
    var showTime: Boolean = true,
    var dateTextSize: Float = DEFAULT_DATE_TEXT_SIZE,
    var timeTextSize: Float = DEFAULT_TIME_TEXT_SIZE,
    var timeZone: String? = null,
    var timeZoneName: String = "",
    var showBackground: Boolean = true
) {
    companion object {
        const val DEFAULT_DATE_TEXT_SIZE = 16f
        const val DEFAULT_TIME_TEXT_SIZE = 52f

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
            80f
        )
    }
}