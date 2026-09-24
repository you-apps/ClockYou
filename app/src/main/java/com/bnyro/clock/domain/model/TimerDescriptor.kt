package com.bnyro.clock.domain.model

import android.os.Parcelable
import androidx.compose.runtime.mutableStateOf
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimerDescriptor(
    var id: Int,
    var settings: TimerSettings
) : Parcelable {
    fun asScheduledObject(): TimerObject {
        return TimerObject(
            id = id,
            label = mutableStateOf(settings.label),
            currentPosition = mutableStateOf(settings.seconds * 1000),
            soundName = settings.soundName,
            soundUri = settings.soundUri,
            soundEnabled = settings.soundEnabled,
            vibrate = settings.vibrate,
            vibrationPattern = settings.vibrationPattern,
            vibrationPatternName = settings.vibrationPatternName,
            incrementSeconds = settings.incrementSeconds
        )
    }
}
