package com.bnyro.clock

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.receivers.PreAlarmReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmSnoozeTest {
    @Before
    fun allowExactAlarms() {
        org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(true)
    }

    @Test
    fun snoozePersistsAndReschedulesAfterReloadWithoutChangingRecurrence() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val repository = app.container.alarmRepository
        val alarm = Alarm(time = 0, enabled = true)
        alarm.id = repository.addAlarm(alarm)
        val nextOccurrence = AlarmHelper.getAlarmTime(alarm)

        AlarmHelper.snooze(app, alarm, 10)
        val snoozed = repository.getAlarmById(alarm.id)!!
        assertNotNull(snoozed.snoozedUntil)
        assertEquals(alarm.time, snoozed.time)
        assertEquals(alarm.startDate, snoozed.startDate)
        assertEquals(snoozed.snoozedUntil, AlarmHelper.getAlarmTime(snoozed))
        assertTrue(snoozed.snoozedUntil!! - System.currentTimeMillis() in 540_000L..600_000L)

        AlarmHelper.enqueue(app, snoozed)
        val manager = app.getSystemService(AlarmManager::class.java)
        assertEquals(snoozed.snoozedUntil, manager.nextAlarmClock.triggerTime)

        CreateUpdateDeleteAlarmUseCase(app, repository).dismissUpcomingAlarm(snoozed)
        val dismissed = repository.getAlarmById(alarm.id)!!
        assertNull(dismissed.snoozedUntil)
        assertEquals(nextOccurrence, AlarmHelper.getAlarmTime(dismissed))
        assertTrue(dismissed.enabled)
    }

    @Test
    fun finalOccurrenceCanSnoozeAndDismissWithoutRearmingTheAlarm() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val repository = app.container.alarmRepository
        val alarm = Alarm(time = 0, enabled = false, endOccurrences = 1)
        alarm.id = repository.addAlarm(alarm)
        AlarmHelper.snooze(app, alarm, 10)
        val snoozed = repository.getAlarmById(alarm.id)!!
        assertEquals(snoozed.snoozedUntil, app.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
        val tomorrow = Alarm(time = 0, enabled = true)
        assertEquals(snoozed, AlarmSortOrder.UPCOMING.sort(listOf(tomorrow, snoozed)).first())

        CreateUpdateDeleteAlarmUseCase(app, repository).dismissUpcomingAlarm(snoozed)
        val dismissed = repository.getAlarmById(alarm.id)!!
        assertFalse(dismissed.enabled)
        assertNull(dismissed.snoozedUntil)
        assertNull(app.getSystemService(AlarmManager::class.java).nextAlarmClock)
    }

    @Test
    fun editingAnAlarmCancelsItsSnooze() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val repository = app.container.alarmRepository
        val alarm = Alarm(time = 0, enabled = true)
        alarm.id = repository.addAlarm(alarm)
        AlarmHelper.snooze(app, alarm)
        val edited = repository.getAlarmById(alarm.id)!!.copy(
            time = LocalTime.now().plusHours(2).toSecondOfDay() / 60 * 60_000L
        )
        CreateUpdateDeleteAlarmUseCase(app, repository).updateAlarm(edited)
        assertNull(repository.getAlarmById(alarm.id)!!.snoozedUntil)
        assertEquals(AlarmHelper.getAlarmTime(edited), app.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
    }

    @Test
    fun snoozedNotificationHasDismissActionEvenForADisabledFinalOccurrence() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val repository = app.container.alarmRepository
        val alarm = Alarm(time = 0, label = "Morning", enabled = false, snoozedUntil = System.currentTimeMillis() + 600_000L)
        alarm.id = repository.addAlarm(alarm)
        app.sendBroadcast(Intent(app, PreAlarmReceiver::class.java).putExtra(AlarmHelper.EXTRA_ID, alarm.id))
        val manager = app.getSystemService(NotificationManager::class.java)
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (manager.activeNotifications.isEmpty()) Thread.sleep(10)
        }
        val notification = manager.activeNotifications.single().notification
        assertEquals("Snoozed alarm", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(app.getString(R.string.dismiss), notification.actions.single().title)
        AlarmHelper.cancel(app, alarm)
        assertTrue(manager.activeNotifications.isEmpty())
    }
}
