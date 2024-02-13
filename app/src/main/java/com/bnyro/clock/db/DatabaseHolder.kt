package com.bnyro.clock.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseHolder {
    private const val dbName = "com.bnyro.clock"
    lateinit var instance: AppDatabase

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE alarms ADD COLUMN label TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE alarms ADD COLUMN soundUri TEXT DEFAULT NULL"
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE alarms ADD COLUMN soundName TEXT DEFAULT NULL"
            )
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE timeZones RENAME TO temp_table")
            database.execSQL("CREATE TABLE IF NOT EXISTS `timeZones` (`zoneId` TEXT NOT NULL, `zoneName` TEXT NOT NULL, `countryName` TEXT NOT NULL, `offset` INTEGER NOT NULL, `key` TEXT NOT NULL, PRIMARY KEY(`key`))")
            database.execSQL("INSERT INTO timeZones (key, zoneId, offset, zoneName, countryName) SELECT name, name, offset, displayName, countryName FROM temp_table")
            database.execSQL("DROP TABLE temp_table")

            postMigrate7to8()
        }
    }

    private fun postMigrate7to8() {
        CoroutineScope(Dispatchers.IO).launch {
            val zones = instance.timeZonesDao().getAll().map {
                it.copy(key = arrayOf(it.zoneId, it.zoneName, it.countryName).joinToString(","))
            }
            instance.timeZonesDao().clear()
            instance.timeZonesDao().insertAll(*zones.toTypedArray())
        }
    }

    fun init(context: Context) {
        instance = Room
            .databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_3_4, MIGRATION_7_8)
            .build()
    }
}
