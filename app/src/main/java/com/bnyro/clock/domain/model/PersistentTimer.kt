package com.bnyro.clock.domain.model

import com.bnyro.clock.util.Preferences

data class PersistentTimer(
    val seconds: Int
) {
    // Write a getter for formattedTime that returns a String in the format of "HH:MM:SS"
    val formattedTime: String
        get() {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val seconds = seconds % 60

            return if (hours == 0) {
                String.format("%02d:%02d", minutes, seconds)
            } else {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }

    companion object {
        private val exampleTimers = listOf(
            60 * 10,
            60 * 15,
            60 * 30,
            60 * 60
        ).map { PersistentTimer(it) }

        fun setTimers(timers: List<PersistentTimer>) {
            val delimitedString = timers.map { it.seconds }.joinToString(",")
            Preferences.edit {
                putString(Preferences.persistentTimerKey, delimitedString)
            }
        }

        fun getTimers(): List<PersistentTimer> {
            val delimitedString =
                Preferences.instance.getString(Preferences.persistentTimerKey, null)
            return delimitedString?.split(",")?.mapNotNull {
                it.toIntOrNull()
            }?.map {
                PersistentTimer(it)
            } ?: exampleTimers
        }
    }
}
