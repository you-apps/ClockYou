package com.bnyro.clock.data.database

import android.content.Context
import android.os.Build
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
    version = 12,
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
        AutoMigration(from = 9, to = 10, spec = AppDatabase.RemoveTimeZoneOffsetColumn::class),
        AutoMigration(from = 10, to = 11)
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

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms RENAME TO temp_table")
                db.execSQL("CREATE TABLE IF NOT EXISTS `alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `time` INTEGER NOT NULL, `label` TEXT, `enabled` INTEGER NOT NULL, `days` TEXT NOT NULL, `vibrate` INTEGER NOT NULL, `soundName` TEXT, `soundUri` TEXT, `snoozeEnabled` INTEGER NOT NULL DEFAULT 1, `snoozeMinutes` INTEGER NOT NULL DEFAULT 10, `soundEnabled` INTEGER NOT NULL DEFAULT 1, `vibrationPattern` TEXT NOT NULL DEFAULT '1000,1000,1000,1000,1000', `vibrationPatternName` TEXT NOT NULL DEFAULT 'Default', `dismissedAt` INTEGER DEFAULT NULL, `startDate` INTEGER NOT NULL DEFAULT 0, `repeatInterval` INTEGER NOT NULL DEFAULT 1, `repeatUnit` TEXT NOT NULL DEFAULT 'WEEK', `repeatAnchor` TEXT NOT NULL DEFAULT 'DAY_OF_MONTH', `repeatDuration` INTEGER DEFAULT NULL, `repeatDurationUnit` TEXT NOT NULL DEFAULT 'DAY', `endDate` INTEGER DEFAULT NULL, `endOccurrences` INTEGER DEFAULT NULL, `advanced` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL(
                    "INSERT INTO alarms (id, time, label, enabled, days, vibrate, soundName, soundUri, snoozeEnabled, snoozeMinutes, soundEnabled, vibrationPattern, vibrationPatternName, dismissedAt, startDate, repeatInterval, repeatUnit, repeatAnchor, repeatDuration, repeatDurationUnit, endDate, endOccurrences, advanced) " +
                        "SELECT id, time, label, enabled, CASE WHEN repeat = 0 OR days = '' THEN '0,1,2,3,4,5,6' ELSE days END, vibrate, soundName, soundUri, snoozeEnabled, snoozeMinutes, soundEnabled, vibrationPattern, vibrationPatternName, dismissedAt, CAST(strftime('%s', 'now', 'localtime') / 86400 AS INTEGER), 1, 'WEEK', 'DAY_OF_MONTH', NULL, 'DAY', NULL, CASE WHEN repeat = 0 THEN 1 ELSE NULL END, 0 FROM temp_table"
                )
                db.execSQL("DROP TABLE temp_table")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val targetContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val ceContext = context.applicationContext
                    val deContext = ceContext.createDeviceProtectedStorageContext()
                    val ceDbFile = ceContext.getDatabasePath(dbName)
                    val deDbFile = deContext.getDatabasePath(dbName)
                    if (ceDbFile.exists()) {
                        if (deDbFile.exists()) {
                            deContext.deleteDatabase(dbName)
                        }
                        deContext.moveDatabaseFrom(ceContext, dbName)
                    }

                    deContext
                } else {
                    context
                }

                val instance = Room
                    .databaseBuilder(targetContext, AppDatabase::class.java, dbName)
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_3_4,
                        MIGRATION_7_8,
                        MIGRATION_11_12
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}