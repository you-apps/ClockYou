package com.bnyro.clock

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.ui.theme.DefaultLabelColor
import com.bnyro.clock.ui.theme.LabelColor
import com.bnyro.clock.ui.theme.SnowLabelColor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LabelColorMigrationTest {
    @Test
    fun version14AlarmsKeepTheirSettingsWhileWhiteBecomesTheThemeDefault() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "label-color-migration"
        val schema = requireNotNull(javaClass.getResourceAsStream(
            "/schemas/com.bnyro.clock.data.database.AppDatabase/14.json"
        )).bufferedReader().use {
            Json.parseToJsonElement(it.readText()).jsonObject.getValue("database").jsonObject
        }
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { legacy ->
            schema.getValue("entities").jsonArray.forEach { entity ->
                val table = entity.jsonObject
                legacy.execSQL(table.getValue("createSql").jsonPrimitive.content.replace(
                    "\${TABLE_NAME}", table.getValue("tableName").jsonPrimitive.content
                ))
            }
            schema.getValue("setupQueries").jsonArray.forEach {
                legacy.execSQL(it.jsonPrimitive.content)
            }
            legacy.execSQL(
                "INSERT INTO alarms (id, time, label, labelColor, enabled, days, vibrate, startDate, repeatInterval, advanced) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(1, 25_200_000L, "Weekdays", SnowLabelColor, 1, "1,2,3,4,5", 1, 20_000, 2, 1)
            )
            legacy.execSQL(
                "INSERT INTO alarms (id, time, label, labelColor, enabled, days, vibrate) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(2, 28_800_000L, "Colored", LabelColor.Cocoa.argb, 0, "0,6", 0)
            )
            legacy.version = schema.getValue("version").jsonPrimitive.int
        }
        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals(
                Alarm(
                    id = 1, time = 25_200_000L, label = "Weekdays", labelColor = DefaultLabelColor,
                    enabled = true, days = listOf(1, 2, 3, 4, 5), startDate = 20_000,
                    repeatInterval = 2, advanced = true
                ),
                upgraded.alarmsDao().findById(1)
            )
            assertEquals(
                Alarm(
                    id = 2, time = 28_800_000L, label = "Colored", labelColor = LabelColor.Cocoa.argb,
                    enabled = false, days = listOf(0, 6), vibrate = false, startDate = 0
                ),
                upgraded.alarmsDao().findById(2)
            )
            val sqlite = upgraded.openHelper.writableDatabase
            sqlite.execSQL("INSERT INTO alarms (id, time, enabled, days, vibrate) VALUES (3, 32400000, 0, '0,1,2,3,4,5,6', 1)")
            assertEquals(DefaultLabelColor, upgraded.alarmsDao().findById(3)?.labelColor)
            val newId = upgraded.alarmsDao().insert(Alarm(time = 36_000_000L))
            assertEquals(DefaultLabelColor, upgraded.alarmsDao().findById(newId)?.labelColor)
        } finally {
            upgraded.close()
            context.deleteDatabase(name)
        }
    }
}
