package com.bnyro.clock

import android.content.Context
import android.os.Looper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableLongStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.util.Preferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import com.bnyro.clock.presentation.screens.alarm.AlarmScreen
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalTime
import java.util.TimeZone
import org.junit.After
import org.junit.Before
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmCountdownTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var timeZone: TimeZone
    private lateinit var activity: ActivityController<ComponentActivity>

    @Before
    fun setUp() {
        timeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val repository = ApplicationProvider.getApplicationContext<App>().container.alarmRepository
        runBlocking { repository.getAlarms().forEach { repository.deleteAlarm(it) } }
        activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
    }

    @After
    fun tearDown() {
        activity.pause().stop().destroy()
        TimeZone.setDefault(timeZone)
    }

    @Test
    fun dismissalFollowsTheSharedClockWithoutAnAlarmEdit() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarm = Alarm(time = LocalTime.now().plusHours(2).toSecondOfDay() / 60 * 60_000L, enabled = true)
        val alarmTime = requireNotNull(AlarmHelper.getAlarmTime(alarm))
        val currentTime = mutableLongStateOf(alarmTime - AlarmHelper.PRE_ALARM_DELAY - 1)
        activity.get().setContent {
            MaterialTheme {
                AlarmItem(
                    alarm = alarm,
                    currentTime = currentTime.longValue,
                    onClick = {},
                    onLongClick = {},
                    onUpdateAlarm = {},
                    onDeleteAlarm = {},
                    onDismissAlarm = {}
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.dismiss)).assertDoesNotExist()
        compose.runOnIdle { currentTime.longValue = alarmTime - AlarmHelper.PRE_ALARM_DELAY }
        compose.onNodeWithText(context.getString(R.string.dismiss)).assertExists()
        compose.runOnIdle { currentTime.longValue = alarmTime }
        compose.onNodeWithText(context.getString(R.string.dismiss)).assertDoesNotExist()
    }

    @Test
    fun upcomingIsDefaultAndDismissalImmediatelyMovesTheFirstAlarm() {
        val application = ApplicationProvider.getApplicationContext<App>()
        Preferences.edit { remove(Preferences.alarmSortOrderKey) }
        val first = Alarm(time = LocalTime.now().plusMinutes(10).toSecondOfDay() / 60 * 60_000L, label = "First", enabled = true)
        val second = Alarm(time = LocalTime.now().plusMinutes(20).toSecondOfDay() / 60 * 60_000L, label = "Second", enabled = true)
        runBlocking {
            application.container.alarmRepository.addAlarm(second)
            application.container.alarmRepository.addAlarm(first)
        }
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        lateinit var model: AlarmModel
        compose.runOnUiThread {
            owner.lifecycle.currentState = Lifecycle.State.RESUMED
            model = AlarmModel(application)
        }
        activity.get().setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                MaterialTheme { AlarmScreen({}, { _, _ -> }, model, SettingsModel()) }
            }
        }
        compose.waitForIdle()
        compose.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            model.alarms.value.size == 2
        }
        assertEquals("First", model.alarms.value.first().label)
        val firstPosition = compose.onNodeWithText("First").fetchSemanticsNode().boundsInRoot.top
        val original = model.alarms.value.first()
        compose.runOnUiThread { model.dismissUpcomingAlarm(original) }
        compose.waitForIdle()
        compose.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            model.alarms.value.first().label == "Second"
        }
        compose.waitForIdle()
        assertEquals(firstPosition, compose.onNodeWithText("Second").fetchSemanticsNode().boundsInRoot.top)
        assertNull(original.dismissedAt)
        compose.runOnUiThread { model.setSortOrder(AlarmSortOrder.LABEL) }
        assertEquals(AlarmSortOrder.LABEL.name, Preferences.instance.getString(Preferences.alarmSortOrderKey, null))
        lateinit var restored: AlarmModel
        compose.runOnUiThread { restored = AlarmModel(application) }
        activity.get().setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                MaterialTheme { AlarmScreen({}, { _, _ -> }, restored, SettingsModel()) }
            }
        }
        compose.waitForIdle()
        compose.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            restored.alarms.value.size == 2
        }
        assertEquals("First", restored.alarms.value.first().label)
    }

    @Test
    fun countdownPausesInBackgroundAndRefreshesOnResume() {
        val application = ApplicationProvider.getApplicationContext<App>()
        val alarm = Alarm(time = LocalTime.now().plusHours(2).toSecondOfDay() / 60 * 60_000L, label = "Countdown")
        runBlocking { application.container.alarmRepository.addAlarm(alarm) }
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        lateinit var model: AlarmModel
        lateinit var settings: SettingsModel
        compose.runOnUiThread {
            owner.lifecycle.currentState = Lifecycle.State.RESUMED
            model = AlarmModel(application)
            settings = SettingsModel()
        }
        activity.get().setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                MaterialTheme {
                    AlarmScreen({}, { _, _ -> }, model, settings)
                }
            }
        }
        compose.waitForIdle()
        compose.waitForIdle()
        compose.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            model.alarms.value.isNotEmpty()
        }
        compose.waitForIdle()
        val initialLabel = application.getString(
            R.string.alarm_starts_in,
            TimeHelper.durationToFormatted(application, (requireNotNull(AlarmHelper.getAlarmTime(alarm)) - System.currentTimeMillis()).milliseconds)
        )
        compose.onNodeWithText(initialLabel).assertExists()
        compose.runOnUiThread { owner.lifecycle.currentState = Lifecycle.State.CREATED }
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"))
        compose.mainClock.advanceTimeBy(2_000)
        compose.onNodeWithText(initialLabel).assertExists()
        compose.runOnUiThread { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        shadowOf(Looper.getMainLooper()).idle()
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        compose.onNodeWithText(initialLabel).assertDoesNotExist()
    }
}
