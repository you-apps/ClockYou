package com.bnyro.clock

import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.BackupTimer
import com.bnyro.clock.domain.model.RepeatAnchor
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.domain.usecase.ClockBackupUseCase
import com.bnyro.clock.util.ClockBackupArchive
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.services.TimerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClockBackupTest {
    private val app = ApplicationProvider.getApplicationContext<App>()

    @Before
    fun clearClockData() = runBlocking {
        app.container.alarmRepository.getAlarms().forEach { app.container.alarmRepository.deleteAlarm(it) }
        app.container.timezoneRepository.replaceAll()
        Preferences.edit { clear() }
        TimerSettings.setSavedTimers(emptyList())
    }

    @Test
    fun archiveRoundTripPreservesEveryAlarmTimerAndClockFieldAndCopiesAudio() = runBlocking {
        val audio = File(app.cacheDir, "selected-sound.ogg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val alarm = Alarm(
            time = 45_600_000, label = "Advanced", labelColor = -123456, enabled = false,
            days = listOf(1, 3, 5), vibrate = false, soundName = "Selected sound",
            soundUri = audio.toUri().toString(), snoozeEnabled = false, snoozeMinutes = 17,
            soundEnabled = false, vibrationPattern = listOf(0, 200, 100, 300),
            vibrationPatternName = "Selected pattern", dismissedAt = 123456789,
            startDate = 25000, repeatInterval = 3, repeatUnit = RepeatUnit.MONTH,
            repeatAnchor = RepeatAnchor.DAY_OF_WEEK, repeatDuration = 4,
            repeatDurationUnit = RepeatUnit.DAY, endDate = 26000, endOccurrences = 12,
            advanced = true
        )
        app.container.alarmRepository.addAlarm(alarm)
        val timer = TimerSettings(
            seconds = 123, label = "Timer", labelColor = -654321,
            soundName = alarm.soundName, soundUri = alarm.soundUri, soundEnabled = false,
            vibrate = false, vibrationPattern = listOf(0, 400, 200),
            vibrationPatternName = "Timer pattern", incrementSeconds = 37
        )
        TimerSettings.setSavedTimers(listOf(timer))
        val zone = TimeZone("Kuala Lumpur", "Asia/Kuala_Lumpur", "Kuala Lumpur", "Malaysia")
        app.container.timezoneRepository.replaceAll(zone)
        Preferences.edit {
            putString(Preferences.alarmSortOrderKey, "UPCOMING")
            putBoolean(Preferences.showSecondsKey, true)
            putInt(Preferences.snoozeTimeMinutesKey, 17)
            putLong("long_setting", 1234567890123)
            putFloat("float_setting", 1.25f)
            putStringSet("set_setting", setOf("a", "b"))
        }
        val useCase = ClockBackupUseCase(app)
        val backup = useCase.capture(listOf(BackupTimer(timer, 73_456)))
        val file = File(app.cacheDir, "backup.zip")
        try {
            ClockBackupArchive.exportBackup(app, file.toUri(), backup)
            audio.delete()
            val restored = ClockBackupArchive.importBackup(app, file.toUri())
            val restoredUri = requireNotNull(restored.alarms.single().soundUri)
            assertEquals(alarm.copy(soundUri = restoredUri), restored.alarms.single())
            assertEquals(timer.copy(soundUri = restoredUri), restored.timers.single())
            assertEquals(BackupTimer(timer.copy(soundUri = restoredUri), 73_456), restored.activeTimers.single())
            assertEquals(zone, restored.timeZones.single())
            assertEquals(backup.preferences, restored.preferences)
            app.contentResolver.openInputStream(restoredUri.toUri())!!.use {
                assertArrayEquals(byteArrayOf(1, 2, 3, 4), it.readBytes())
            }
            assertEquals(restored, ClockBackupArchive.importBackup(app, file.toUri()))
        } finally {
            audio.delete()
            file.delete()
        }
    }

    @Test
    fun importMergesItemsAndAppliesSettingsWithoutChangingOtherPreferenceStores() = runBlocking {
        val useCase = ClockBackupUseCase(app)
        app.container.alarmRepository.addAlarm(Alarm(time = 1000, label = "Imported"))
        TimerSettings.setSavedTimers(listOf(TimerSettings(seconds = 42)))
        Preferences.edit { putString(Preferences.themeKey, "DARK") }
        val backup = useCase.capture(emptyList())
        clearClockData()
        app.container.alarmRepository.addAlarm(Alarm(time = 2000, label = "Existing"))
        TimerSettings.setSavedTimers(listOf(TimerSettings(seconds = 84)))
        val unrelated = app.getSharedPreferences("jay_identity", 0)
        unrelated.edit().putString("identity", "keep").commit()
        val widgets = app.getSharedPreferences("widgets", 0)
        widgets.edit().putInt("widget", 7).commit()
        useCase.restore(backup)
        useCase.restore(backup)
        assertEquals(setOf("Imported", "Existing"), app.container.alarmRepository.getAlarms().map { it.label }.toSet())
        assertEquals(setOf(42, 84), TimerSettings.getSavedTimers().map { it.seconds }.toSet())
        assertEquals("DARK", Preferences.instance.getString(Preferences.themeKey, null))
        assertEquals("keep", unrelated.getString("identity", null))
        assertEquals(7, widgets.getInt("widget", 0))
    }

    @Test
    fun activeTimersRestorePausedWithRemainingTimeAndOriginalDuration() {
        val service = Robolectric.buildService(TimerService::class.java).create()
        try {
            service.get().restoreTimers(listOf(BackupTimer(TimerSettings(seconds = 123), 73_456)))
            val timer = service.get().timerObjects.single()
            assertEquals(WatchState.PAUSED, timer.state.value)
            assertEquals(73_456, timer.currentPosition.value)
            assertEquals(123_000, timer.initialPosition.value)
        } finally {
            service.destroy()
        }
    }
}
