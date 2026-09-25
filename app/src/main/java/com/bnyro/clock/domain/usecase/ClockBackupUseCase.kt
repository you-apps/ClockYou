package com.bnyro.clock.domain.usecase

import android.content.Context
import androidx.room.withTransaction
import com.bnyro.clock.App
import com.bnyro.clock.domain.model.BackupPreference
import com.bnyro.clock.domain.model.BackupTimer
import com.bnyro.clock.domain.model.ClockBackup
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.Preferences
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class ClockBackupUseCase(private val context: Context) {
    private val container = (context.applicationContext as App).container

    suspend fun capture(activeTimers: List<BackupTimer>): ClockBackup {
        val preferences = Preferences.instance.all.filterKeys { it != Preferences.savedTimersKey }
            .mapValues { (_, value) ->
                when (value) {
                    is Boolean -> BackupPreference("boolean", JsonPrimitive(value))
                    is Int -> BackupPreference("int", JsonPrimitive(value))
                    is Long -> BackupPreference("long", JsonPrimitive(value))
                    is Float -> BackupPreference("float", JsonPrimitive(value))
                    is String -> BackupPreference("string", JsonPrimitive(value))
                    is Set<*> -> BackupPreference("strings", JsonArray(value.map { JsonPrimitive(it as String) }))
                    else -> error("Unsupported preference")
                }
            }
        return ClockBackup(
            alarms = container.alarmRepository.getAlarms().map { it.copy(id = 0, snoozedUntil = null) },
            timers = TimerSettings.getSavedTimers().map { it.copy(id = 0) },
            activeTimers = activeTimers,
            timeZones = container.timezoneRepository.getTimezones(),
            preferences = preferences
        )
    }

    suspend fun restore(backup: ClockBackup) {
        require(backup.version == 1)
        val preferences = backup.preferences?.mapValues { (_, preference) ->
            when (preference.type) {
                "boolean" -> preference.value.jsonPrimitive.boolean
                "int" -> preference.value.jsonPrimitive.int
                "long" -> preference.value.jsonPrimitive.long
                "float" -> preference.value.jsonPrimitive.float
                "string" -> preference.value.jsonPrimitive.content
                "strings" -> preference.value.jsonArray.map { it.jsonPrimitive.content }.toSet()
                else -> error("Unsupported preference")
            }
        }
        val timers = (TimerSettings.getSavedTimers().map { it.copy(id = 0) } + backup.timers)
            .distinct()
        val addedAlarms = container.database.withTransaction {
            val existingAlarms = container.alarmRepository.getAlarms().map { it.copy(id = 0, snoozedUntil = null) }.toSet()
            val added = backup.alarms.map { it.copy(id = 0, snoozedUntil = null) }.distinct()
                .filter { it !in existingAlarms }.map { alarm ->
                    alarm.copy(id = container.alarmRepository.addAlarm(alarm))
                }
            val timeZones = (container.timezoneRepository.getTimezones() + backup.timeZones)
                .distinctBy { it.key }
            container.timezoneRepository.replaceAll(*timeZones.toTypedArray())
            added
        }
        if (preferences != null) {
            Preferences.edit {
                clear()
                preferences.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> putBoolean(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                        is String -> putString(key, value)
                        is Set<*> -> putStringSet(key, value.map { it as String }.toSet())
                    }
                }
            }
        }
        TimerSettings.setSavedTimers(timers)
        addedAlarms.forEach { AlarmHelper.enqueue(context, it) }
    }
}
