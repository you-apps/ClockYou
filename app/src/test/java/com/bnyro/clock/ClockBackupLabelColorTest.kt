package com.bnyro.clock

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.BackupTimer
import com.bnyro.clock.domain.model.ClockBackup
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.ui.theme.DefaultLabelColor
import com.bnyro.clock.ui.theme.LabelColor
import com.bnyro.clock.ui.theme.SnowLabelColor
import com.bnyro.clock.util.ClockBackupFile
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ClockBackupLabelColorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var file: File

    @Before
    fun createBackupFile() {
        file = File.createTempFile("clock-backup-colors", ".json", context.cacheDir)
    }

    @After
    fun removeBackupFile() {
        file.delete()
    }

    @Test
    fun version1WhiteBecomesDefaultAcrossAlarmsSavedTimersAndActiveTimers() {
        val legacy = ClockBackup(
            version = 1,
            alarms = listOf(
                Alarm(time = 25_200_000L, labelColor = SnowLabelColor),
                Alarm(time = 28_800_000L, labelColor = LabelColor.Cocoa.argb)
            ),
            timers = listOf(
                TimerSettings(seconds = 60, labelColor = SnowLabelColor),
                TimerSettings(seconds = 120, labelColor = LabelColor.Cocoa.argb)
            ),
            activeTimers = listOf(
                BackupTimer(TimerSettings(seconds = 60, labelColor = SnowLabelColor), 30_000),
                BackupTimer(TimerSettings(seconds = 120, labelColor = LabelColor.Cocoa.argb), 90_000)
            ),
            timeZones = emptyList(),
            preferences = null
        )
        file.writeText(Json { encodeDefaults = true }.encodeToString(legacy))

        val restored = ClockBackupFile.importBackup(context, Uri.fromFile(file))

        assertEquals(listOf(DefaultLabelColor, LabelColor.Cocoa.argb), restored.alarms.map { it.labelColor })
        assertEquals(listOf(DefaultLabelColor, LabelColor.Cocoa.argb), restored.timers.map { it.labelColor })
        assertEquals(listOf(DefaultLabelColor, LabelColor.Cocoa.argb), restored.activeTimers.map { it.settings.labelColor })
        assertEquals(listOf(30_000, 90_000), restored.activeTimers.map { it.remainingMillis })
    }

    @Test
    fun newBackupsKeepExplicitWhiteSeparateFromTheThemeDefault() {
        val backup = ClockBackup(
            alarms = listOf(
                Alarm(time = 25_200_000L),
                Alarm(time = 28_800_000L, labelColor = SnowLabelColor),
                Alarm(time = 32_400_000L, labelColor = LabelColor.Cocoa.argb)
            ),
            timers = listOf(
                TimerSettings(seconds = 60),
                TimerSettings(seconds = 120, labelColor = SnowLabelColor),
                TimerSettings(seconds = 180, labelColor = LabelColor.Cocoa.argb)
            ),
            activeTimers = listOf(
                BackupTimer(TimerSettings(seconds = 60), 30_000),
                BackupTimer(TimerSettings(seconds = 120, labelColor = SnowLabelColor), 90_000),
                BackupTimer(TimerSettings(seconds = 180, labelColor = LabelColor.Cocoa.argb), 150_000)
            ),
            timeZones = emptyList(),
            preferences = null
        )

        ClockBackupFile.exportBackup(context, Uri.fromFile(file), backup)

        assertEquals(2, Json.parseToJsonElement(file.readText()).jsonObject.getValue("version").jsonPrimitive.int)
        assertEquals(backup, ClockBackupFile.importBackup(context, Uri.fromFile(file)))
        assertEquals(DefaultLabelColor, backup.alarms.first().labelColor)
        assertEquals(DefaultLabelColor, backup.timers.first().labelColor)
        assertEquals(DefaultLabelColor, backup.activeTimers.first().settings.labelColor)
    }
}
