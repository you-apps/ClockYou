package com.bnyro.clock.util

import android.content.Context
import android.net.Uri
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.BackupTimer
import com.bnyro.clock.domain.model.ClockBackup
import com.bnyro.clock.domain.model.TimerSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.time.LocalDate

object ClockBackupFile {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun exportBackup(context: Context, uri: Uri, backup: ClockBackup) {
        requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use {
            it.write(json.encodeToString(backup.withoutCustomAudio()))
        }
    }

    fun importBackup(context: Context, uri: Uri): ClockBackup {
        val root = requireNotNull(context.contentResolver.openInputStream(uri)).bufferedReader().use {
            json.parseToJsonElement(it.readText()).jsonObject
        }
        val backup = if ("version" in root) {
            json.decodeFromJsonElement<ClockBackup>(root)
        } else {
            json.decodeFromJsonElement<FossifyBackup>(root).toClockBackup()
        }
        require(backup.version == 1)
        return backup.withoutCustomAudio()
    }
}

@Serializable
private data class FossifyBackup(
    val alarms: List<FossifyAlarm>,
    val timers: List<FossifyTimer>
) {
    fun toClockBackup(): ClockBackup {
        val today = LocalDate.now()
        val savedTimers = timers.filterNot { it.oneShot }.map { timer ->
            TimerSettings(seconds = timer.seconds, label = timer.label, vibrate = timer.vibrate)
        }
        val activeTimers = timers.mapNotNull { timer ->
            val state = timer.state.getValue("type").jsonPrimitive.content.substringAfterLast('.')
            val remainingMillis = when (state) {
                "Running", "Paused" -> timer.state.getValue("tick").jsonPrimitive.long
                    .coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
                "Idle", "Finished" -> null
                else -> error("Unsupported Fossify timer state")
            }
            remainingMillis?.let {
                BackupTimer(
                    settings = TimerSettings(seconds = timer.seconds, label = timer.label, vibrate = timer.vibrate),
                    remainingMillis = it
                )
            }
        }
        return ClockBackup(
            alarms = alarms.map { alarm ->
                val date = when (alarm.days) {
                    -1 -> today
                    -2 -> today.plusDays(1)
                    else -> null
                }
                val days = date?.let { listOf(it.dayOfWeek.value % 7) }
                    ?: (0..6).filter { day -> alarm.days and (1 shl ((day + 6) % 7)) != 0 }
                Alarm(
                    time = alarm.timeInMinutes * 60_000L,
                    label = alarm.label,
                    enabled = alarm.isEnabled,
                    days = days,
                    vibrate = alarm.vibrate,
                    startDate = date?.toEpochDay() ?: today.toEpochDay(),
                    endOccurrences = 1.takeIf { date != null || alarm.oneShot }
                )
            },
            timers = savedTimers,
            activeTimers = activeTimers,
            timeZones = emptyList(),
            preferences = null
        )
    }
}

@Serializable
private data class FossifyAlarm(
    val id: Int,
    val timeInMinutes: Int,
    val days: Int,
    val isEnabled: Boolean,
    val vibrate: Boolean,
    val soundTitle: String,
    val soundUri: String,
    val label: String,
    val oneShot: Boolean = false
)

@Serializable
private data class FossifyTimer(
    val id: Int?,
    val seconds: Int,
    val state: JsonObject,
    val vibrate: Boolean,
    val soundUri: String,
    val soundTitle: String,
    val label: String,
    val createdAt: Long,
    val channelId: String? = null,
    val oneShot: Boolean = false
)
