package com.bnyro.clock.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bnyro.clock.db.dao.AlarmsDao
import com.bnyro.clock.db.dao.Converters
import com.bnyro.clock.db.dao.TimeZonesDao
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.obj.TimeZone

@Database(
    entities = [TimeZone::class, Alarm::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeZonesDao(): TimeZonesDao
    abstract fun alarmsDao(): AlarmsDao
}
