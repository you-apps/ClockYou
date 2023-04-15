package com.bnyro.clock.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    fun init(context: Context) {
        instance = Room
            .databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_3_4)
            .build()
    }
}
