package com.bnyro.clock.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bnyro.clock.data.database.dao.AlarmsDao
import com.bnyro.clock.data.database.dao.Converters
import com.bnyro.clock.data.database.dao.TimeZonesDao
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.TimeZone

@Database(
    entities = [TimeZone::class, Alarm::class],
    version = 10,
    autoMigrations = [
        AutoMigration(
            from = 2,
            to = 3,
            spec = AppDatabase.RemoveSoundColumnAutoMigration::class
        ),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10, spec = AppDatabase.RemoveTimeZoneOffsetColumn::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    @DeleteColumn("alarms", "sound")
    class RemoveSoundColumnAutoMigration : AutoMigrationSpec

    @DeleteColumn(tableName = "timeZones", columnName = "offset")
    class RemoveTimeZoneOffsetColumn : AutoMigrationSpec

    abstract fun timeZonesDao(): TimeZonesDao
    abstract fun alarmsDao(): AlarmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val dbName = "com.bnyro.clock"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE alarms ADD COLUMN label TEXT DEFAULT NULL"
                )
                db.execSQL(
                    "ALTER TABLE alarms ADD COLUMN soundUri TEXT DEFAULT NULL"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE alarms ADD COLUMN soundName TEXT DEFAULT NULL"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timeZones RENAME TO temp_table")
                db.execSQL("CREATE TABLE IF NOT EXISTS `timeZones` (`zoneId` TEXT NOT NULL, `zoneName` TEXT NOT NULL, `countryName` TEXT NOT NULL, `offset` INTEGER NOT NULL, `key` TEXT NOT NULL, PRIMARY KEY(`key`))")
                db.execSQL("INSERT INTO timeZones (key, zoneId, offset, zoneName, countryName) SELECT name || ',' || displayName || ',' || countryName, name, offset, displayName, countryName FROM temp_table")
                db.execSQL("DROP TABLE temp_table")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(context, AppDatabase::class.java, dbName)
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_3_4,
                        MIGRATION_7_8
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
