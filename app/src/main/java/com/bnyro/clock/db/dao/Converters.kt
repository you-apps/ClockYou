package com.bnyro.clock.db.dao

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun stringToIntList(value: String): List<Int> = value.split(",").mapNotNull { it.toIntOrNull() }

    @TypeConverter
    fun intListToString(value: List<Int>) = value.joinToString(",")
}
