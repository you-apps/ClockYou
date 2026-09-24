package com.bnyro.clock.domain.model

import android.os.Parcelable
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * Everything a timer is started with, and everything a saved timer keeps between runs.
 *
 * @property id Tells the saved timers apart while they are listed, and is handed out on
 * reading them rather than kept, since it means nothing to a timer that is only being started.
 * @property seconds The duration the timer counts down from.
 * @property label The name of the timer, which falls back to the duration it was set to.
 */
@Parcelize
@Serializable
data class TimerSettings(
    @Transient val id: Int = 0,
    val seconds: Int,
    val label: String = TimeHelper.durationToName(seconds),
    val soundName: String? = null,
    val soundUri: String? = null,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val vibrationPattern: List<Int> = listOf(0, 1000, 1000, 1000, 1000),
    val vibrationPatternName: String = "Default",
    val incrementSeconds: Int? = null
) : Parcelable {
    companion object {
        private val exampleTimers = listOf(
            60 * 5,
            60 * 10,
            60 * 15,
            60 * 30
        ).mapIndexed { index, seconds -> TimerSettings(id = index + 1, seconds = seconds) }

        fun setSavedTimers(timers: List<TimerSettings>) {
            Preferences.edit {
                putString(Preferences.savedTimersKey, Json.encodeToString(timers))
            }
        }

        fun getSavedTimers(): List<TimerSettings> {
            val savedTimers = Preferences.instance.getString(Preferences.savedTimersKey, null)
                ?: return exampleTimers
            return Json.decodeFromString<List<TimerSettings>>(savedTimers)
                .mapIndexed { index, timer -> timer.copy(id = index + 1) }
        }
    }
}
