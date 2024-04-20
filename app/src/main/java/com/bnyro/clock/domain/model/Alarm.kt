package com.bnyro.clock.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bnyro.clock.util.TimeHelper

/**
 * @property time The time of the day in milliseconds.
 * @property days The days of the week to ring the alarm. Sunday-0, Monday-1 ,... ,Saturday-6
 * @property snoozeMinutes How long the snooze should last in minutes (default 10).
 */
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var time: Long,
    var label: String? = null,
    var enabled: Boolean = false,
    var days: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    var vibrate: Boolean = false,
    var soundName: String? = null,
    var soundUri: String? = null,
    @ColumnInfo(defaultValue = "1") var repeat: Boolean = false,
    @ColumnInfo(defaultValue = "1") var snoozeEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "10") var snoozeMinutes: Int = 10,
    @ColumnInfo(defaultValue = "1") var soundEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1000,1000,1000,1000,1000") var vibrationPattern: List<Int> = List(5) { 1000 },
    @ColumnInfo(defaultValue = "Default") var vibrationPatternName: String = "Default",
) {
    @Ignore
    val isWeekends: Boolean = days == listOf(0, 6)

    @Ignore
    val isWeekdays: Boolean = days == listOf(1, 2, 3, 4, 5)

    @Ignore
    val isRepeatEveryday: Boolean = days.size == 7

    @Ignore
    val formattedTime: String = TimeHelper.millisToFormatted(time)
}
