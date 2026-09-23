package com.bnyro.clock

import android.media.AudioManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarm.AlarmAlertScreen
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.presentation.screens.settings.components.AlarmVolumePreference
import com.bnyro.clock.util.TimeHelper
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmPresentationTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun ringingShowsCurrentTimeAndScheduledTimeAsFallbackLabel() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val context = activity.get()
        val alarmTime = 7 * 3_600_000L + 30 * 60_000L
        try {
            context.setContent {
                AlarmAlertScreen(
                    onDismiss = {}, onSnooze = {}, label = "  ",
                    snoozeEnabled = true, snoozeTime = 10, alarmTimeMillis = alarmTime
                )
            }
            compose.onNodeWithText(TimeHelper.formatTime(context, TimeHelper.getTimeByZone())).assertExists()
            compose.onNodeWithText(context.getString(R.string.alarm_time_label, TimeHelper.millisToFormatted(context, alarmTime))).assertExists()
        } finally {
            activity.pause().stop().destroy()
        }
    }

    @Test
    fun snoozedFinalOccurrenceShowsDismissAndUpdatesCountdown() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val context = activity.get()
        val now = mutableLongStateOf(System.currentTimeMillis())
        val alarm = Alarm(time = 0, enabled = false, snoozedUntil = now.longValue + 600_000L)
        try {
            context.setContent {
                MaterialTheme {
                    AlarmItem(
                        alarm = alarm, currentTime = now.longValue,
                        onClick = {}, onLongClick = {}, onUpdateAlarm = {},
                        onDeleteAlarm = {}, onDismissAlarm = {}
                    )
                }
            }
            compose.onNodeWithText(context.getString(R.string.dismiss)).assertExists()
            compose.onNodeWithText(context.getString(R.string.alarm_snoozed_for, TimeHelper.durationToFormatted(context, 600_000L.milliseconds))).assertExists()
            compose.runOnIdle { now.longValue += 60_000L }
            compose.onNodeWithText(context.getString(R.string.alarm_snoozed_for, TimeHelper.durationToFormatted(context, 540_000L.milliseconds))).assertExists()
        } finally {
            activity.pause().stop().destroy()
        }
    }

    @Test
    fun volumeSliderChangesOnlyTheAlarmStreamAndReadsExternalChanges() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val app = ApplicationProvider.getApplicationContext<App>()
        val manager = app.getSystemService(AudioManager::class.java)
        val mediaVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
        try {
            activity.get().setContent { MaterialTheme { AlarmVolumePreference() } }
            compose.onNodeWithContentDescription(app.getString(R.string.alarm_volume))
                .performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
            compose.runOnIdle {
                assertEquals(3, manager.getStreamVolume(AudioManager.STREAM_ALARM))
                assertEquals(mediaVolume, manager.getStreamVolume(AudioManager.STREAM_MUSIC))
                manager.setStreamVolume(AudioManager.STREAM_ALARM, 5, 0)
                app.contentResolver.notifyChange(Settings.System.CONTENT_URI, null)
            }
            compose.waitForIdle()
            val node = compose.onNodeWithContentDescription(app.getString(R.string.alarm_volume)).fetchSemanticsNode()
            assertEquals(5f, node.config[androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo].current)
        } finally {
            activity.pause().stop().destroy()
        }
    }
}
