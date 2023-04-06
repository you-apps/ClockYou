package com.bnyro.clock.obj

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var time: Long,
    var enabled: Boolean = false,
    var days: List<Int> = listOf(1),
    var sound: String? = null,
    var vibrate: Boolean = false
)
