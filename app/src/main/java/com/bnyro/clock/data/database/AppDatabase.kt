package com.bnyro.clock.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.bnyro.clock.data.database.dao.AlarmsDao
import com.bnyro.clock.data.database.dao.Converters
import com.bnyro.clock.data.database.dao.TimeZonesDao
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.TimeZone

@Database(
    entities = [TimeZone::class, Alarm::class],
    version = 8,
    autoMigrations = [
        AutoMigration(
            from = 2,
            to = 3,
            spec = AppDatabase.RemoveSoundColumnAutoMigration::class
        ),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    @DeleteColumn("alarms", "sound")
    class RemoveSoundColumnAutoMigration : AutoMigrationSpec

    abstract fun timeZonesDao(): TimeZonesDao
    abstract fun alarmsDao(): AlarmsDao
}
