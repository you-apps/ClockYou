package com.bnyro.clock

import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.Intent
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarm.AlarmActivity
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.receivers.AlarmReceiver
import com.bnyro.clock.util.services.AlarmService
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = App::class, instrumentedPackages = ["com.bnyro.clock.util"])
class AlarmSnoozeAnchorTest {
    private lateinit var context: App
    private lateinit var database: AppDatabase
    private lateinit var alarm: Alarm
    private val ringTime = Instant.parse("2026-10-14T09:00:00Z").toEpochMilli()

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        Preferences.init(context)
        Preferences.instance.edit().clear().commit()
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime - 60_000L))
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        context.container = AppContainer(database)
        val scheduled = Instant.ofEpochMilli(ringTime).atZone(ZoneId.systemDefault())
        alarm = Alarm(
            time = scheduled.hour * 3_600_000L + scheduled.minute * 60_000L,
            enabled = true,
            snoozeMinutes = 5,
            soundEnabled = false,
            vibrate = false
        )
        alarm.id = context.container.alarmRepository.addAlarm(alarm)
        Unit
    }

    @After
    fun tearDown() {
        AlarmHelper.cancel(context, alarm)
        database.close()
        Preferences.instance.edit().clear().commit()
    }

    @Test
    fun snoozingAfterTheRingStartsKeepsTheScheduledFiveMinuteTarget() = runBlocking {
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime + 100_000L))

        val target = AlarmHelper.snooze(context, alarm, ringTime, 5)

        assertEquals(ringTime + 5 * 60_000L, target)
        assertEquals(target, context.container.alarmRepository.getAlarmById(alarm.id)?.snoozedUntil)
        assertEquals(target, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
    }

    @Test
    fun snoozingAfterTheTargetHasPassedUsesTheNextClockMinute() = runBlocking {
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime + 400_000L))

        val target = AlarmHelper.snooze(context, alarm, ringTime, 5)

        assertEquals(ringTime + 7 * 60_000L, target)
        assertEquals(target, context.container.alarmRepository.getAlarmById(alarm.id)?.snoozedUntil)
        assertEquals(target, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
    }

    @Test
    fun snoozingAtTheTargetBoundaryUsesTheNextClockMinute() = runBlocking {
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime + 5 * 60_000L))

        val target = AlarmHelper.snooze(context, alarm, ringTime, 5)

        assertEquals(ringTime + 6 * 60_000L, target)
        assertEquals(target, context.container.alarmRepository.getAlarmById(alarm.id)?.snoozedUntil)
        assertEquals(target, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
    }

    @Test
    fun snoozingAgainStartsFromTheCurrentSnoozedRing() = runBlocking {
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime + 100_000L))
        val firstTarget = AlarmHelper.snooze(context, alarm, ringTime, 5)
        assertTrue(SystemClock.setCurrentTimeMillis(firstTarget + 100_000L))
        val snoozed = requireNotNull(context.container.alarmRepository.getAlarmById(alarm.id))

        val secondTarget = AlarmHelper.snooze(context, snoozed, firstTarget, 5)

        assertEquals(ringTime + 10 * 60_000L, secondTarget)
        assertEquals(secondTarget, context.container.alarmRepository.getAlarmById(alarm.id)?.snoozedUntil)
        assertEquals(secondTarget, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
    }

    @Test
    fun lateNativeDeliveryCarriesItsRingAnchorThroughTheNotificationSnooze() = runBlocking {
        AlarmHelper.enqueue(context, alarm)
        val scheduled = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
            .single { shadowOf(it.operation).savedIntent.component?.className == AlarmReceiver::class.java.name }
        val nativeRing = scheduled.triggerAtTime
        val delivery = Intent(shadowOf(scheduled.operation).savedIntent)
        assertEquals(nativeRing, delivery.getLongExtra(AlarmHelper.EXTRA_TRIGGER_TIME, -1L))
        assertTrue(SystemClock.setCurrentTimeMillis(nativeRing + 100_000L))

        AlarmReceiver().onReceive(context, delivery)
        val serviceIntent = requireNotNull(shadowOf(context).nextStartedService)
        assertEquals(nativeRing, serviceIntent.getLongExtra(AlarmHelper.EXTRA_TRIGGER_TIME, -1L))
        val controller = Robolectric.buildService(AlarmService::class.java).create()
        try {
            controller.get().onStartCommand(serviceIntent, 0, 1)
            assertEquals(nativeRing, requireNotNull(shadowOf(context).nextStartedActivity)
                .getLongExtra(AlarmHelper.EXTRA_TRIGGER_TIME, -1L))
            val notification = requireNotNull(shadowOf(controller.get()).lastForegroundNotification)
            assertEquals(nativeRing, shadowOf(notification.contentIntent).savedIntent
                .getLongExtra(AlarmHelper.EXTRA_TRIGGER_TIME, -1L))

            notification.actions.single { it.title == context.getString(R.string.snooze) }.actionIntent.send()
            shadowOf(Looper.getMainLooper()).idle()

            val snoozed = requireNotNull(context.container.alarmRepository.getAlarmById(alarm.id))
            assertEquals(nativeRing + 5 * 60_000L, snoozed.snoozedUntil)
            assertEquals(snoozed.snoozedUntil, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
            assertTrue(shadowOf(controller.get()).isStoppedBySelf)
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun activityReentrySnoozesFromTheNewRingRatherThanItsPreviousIntent() = runBlocking {
        assertTrue(SystemClock.setCurrentTimeMillis(ringTime + 100_000L))
        shadowOf(context.getSystemService(KeyguardManager::class.java)).setKeyguardLocked(true)
        shadowOf(context).grantPermissions("${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
        val controller = Robolectric.buildActivity(
            AlarmActivity::class.java,
            Intent(context, AlarmActivity::class.java)
                .putExtra(AlarmHelper.EXTRA_ID, alarm.id)
                .putExtra(AlarmHelper.EXTRA_TRIGGER_TIME, ringTime)
        ).create()
        try {
            val nextRing = ringTime + 5 * 60_000L
            assertTrue(SystemClock.setCurrentTimeMillis(nextRing + 100_000L))
            controller.newIntent(Intent(context, AlarmActivity::class.java)
                .putExtra(AlarmHelper.EXTRA_ID, alarm.id)
                .putExtra(AlarmHelper.EXTRA_TRIGGER_TIME, nextRing))

            assertTrue(controller.get().onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN, null))

            val snoozed = requireNotNull(context.container.alarmRepository.getAlarmById(alarm.id))
            assertEquals(ringTime + 10 * 60_000L, snoozed.snoozedUntil)
            assertEquals(snoozed.snoozedUntil, context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime)
            assertTrue(controller.get().isFinishing)
        } finally {
            controller.destroy()
        }
    }
}
