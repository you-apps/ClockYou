package com.bnyro.clock.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ClockBackup(
    val version: Int = 1,
    val alarms: List<Alarm>,
    val timers: List<TimerSettings>,
    val activeTimers: List<BackupTimer>,
    val timeZones: List<TimeZone>,
    val preferences: Map<String, BackupPreference>,
    val sounds: Map<String, String> = emptyMap()
)

@Serializable
data class BackupTimer(
    val settings: TimerSettings,
    val remainingMillis: Int
)

@Serializable
data class BackupPreference(
    val type: String,
    val value: JsonElement
)
