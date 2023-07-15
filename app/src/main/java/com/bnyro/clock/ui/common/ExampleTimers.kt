package com.bnyro.clock.ui.common

data class ExampleTimer(
    val seconds: Int,
) {
    // Write a getter for formattedTime that returns a String in the format of "HH:MM:SS"
    val formattedTime: String
        get() {
            val hours = seconds / 3600;
            val minutes = (seconds % 3600) / 60;
            val seconds = seconds % 60;

            return "%02d:%02d:%02d".format(hours, minutes, seconds);
        }
}


val EXAMPLE_TIMERS = listOf<ExampleTimer>(
    // 1 Minute
    ExampleTimer(seconds = 60),
    // 2 Minutes
    ExampleTimer(seconds = 60 * 2),
    // 5 Minutes
    ExampleTimer(seconds = 60 * 5),
    // 10 Minutes
    ExampleTimer(seconds = 60 * 10),
    // 15 Minutes
    ExampleTimer(seconds = 60 * 15),
    // 20 Minutes
    ExampleTimer(seconds = 60 * 20),
    // 30 Minutes
    ExampleTimer(seconds = 60 * 30),
    // 60 Minutes
    ExampleTimer(seconds = 60 * 60),
    // 90 Minutes
    ExampleTimer(seconds = 60 * 90),
)
