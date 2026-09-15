package com.bnyro.clock.data.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {
    @Test
    fun migrate12To13MovesTheOldDefaultVibrationOntoTheNewOne() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val v12Helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DB)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(12) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                "CREATE TABLE IF NOT EXISTS `timeZones` (`key` TEXT NOT NULL, `zoneId` TEXT NOT NULL, `zoneName` TEXT NOT NULL, `countryName` TEXT NOT NULL, PRIMARY KEY(`key`))"
                            )
                            db.execSQL(
                                "CREATE TABLE IF NOT EXISTS `alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `time` INTEGER NOT NULL, `label` TEXT, `enabled` INTEGER NOT NULL, `days` TEXT NOT NULL, `vibrate` INTEGER NOT NULL, `soundName` TEXT, `soundUri` TEXT, `snoozeEnabled` INTEGER NOT NULL DEFAULT 1, `snoozeMinutes` INTEGER NOT NULL DEFAULT 10, `soundEnabled` INTEGER NOT NULL DEFAULT 1, `vibrationPattern` TEXT NOT NULL DEFAULT '1000,1000,1000,1000,1000', `vibrationPatternName` TEXT NOT NULL DEFAULT 'Default', `dismissedAt` INTEGER DEFAULT NULL, `startDate` INTEGER NOT NULL DEFAULT 0, `repeatInterval` INTEGER NOT NULL DEFAULT 1, `repeatUnit` TEXT NOT NULL DEFAULT 'WEEK', `repeatAnchor` TEXT NOT NULL DEFAULT 'DAY_OF_MONTH', `repeatDuration` INTEGER DEFAULT NULL, `repeatDurationUnit` TEXT NOT NULL DEFAULT 'DAY', `endDate` INTEGER DEFAULT NULL, `endOccurrences` INTEGER DEFAULT NULL, `advanced` INTEGER NOT NULL DEFAULT 0)"
                            )
                            db.execSQL(
                                "INSERT INTO alarms (id, time, enabled, days, vibrate, vibrationPattern, vibrationPatternName) VALUES " +
                                    "(1, 0, 1, '0', 1, '1000,1000,1000,1000,1000', 'Default'), " +
                                    "(2, 0, 1, '0', 1, '1000,1000,1000,1000,1000', 'Heartbeat'), " +
                                    "(3, 0, 1, '0', 1, '500,500,500', 'Custom')"
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) = Unit
                    }
                )
                .build()
        )
        v12Helper.writableDatabase
        v12Helper.close()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_12_13)
            .build()
        val alarms = runBlocking { db.alarmsDao().getAll() }
        db.close()

        assertEquals(
            listOf(
                listOf(0, 1000, 1000, 1000, 1000),
                listOf(1000, 1000, 1000, 1000, 1000),
                listOf(500, 500, 500)
            ),
            alarms.sortedBy { it.id }.map { it.vibrationPattern }
        )
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
