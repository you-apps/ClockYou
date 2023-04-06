package com.bnyro.clock.obj

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var time: Long,
    var enabled: Boolean = false,
    var days: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    var sound: String? = null,
    var vibrate: Boolean = false
)
