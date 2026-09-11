package com.bnyro.clock.domain.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bnyro.clock.util.Preferences
import kotlin.math.ceil

data class TimerObject(
    var id: Int = 0,
    var label: MutableState<String> = mutableStateOf(""),
    var currentPosition: MutableState<Int> = mutableStateOf(0),
    var initialPosition: MutableState<Int> = mutableStateOf(currentPosition.value),
    var state: MutableState<WatchState> = mutableStateOf(WatchState.IDLE),
    var soundName: String? = null,
    var soundUri: String? = null,
    var soundEnabled: Boolean = true,
    var vibrate: Boolean = true,
    var vibrationPattern: List<Int> = listOf(0, 1000, 1000, 1000, 1000),
    var vibrationPatternName: String = "Default",
    var incrementSeconds: Int? = null
) {
    val secondsLeft: Int
        get() = ceil(currentPosition.value / 1000.0).toInt()

    val effectiveIncrementSeconds: Int
        get() = incrementSeconds ?: Preferences.instance.getInt(
            Preferences.timerIncrementSecondsKey,
            60
        )

    val settings: TimerSettings
        get() = TimerSettings(
            seconds = initialPosition.value / 1000,
            label = label.value,
            soundName = soundName,
            soundUri = soundUri,
            soundEnabled = soundEnabled,
            vibrate = vibrate,
            vibrationPattern = vibrationPattern,
            vibrationPatternName = vibrationPatternName,
            incrementSeconds = incrementSeconds
        )
}
