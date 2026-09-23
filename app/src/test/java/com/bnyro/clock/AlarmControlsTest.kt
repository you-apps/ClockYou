package com.bnyro.clock

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.ui.theme.DefaultLabelColor
import com.bnyro.clock.ui.theme.LabelColor
import com.bnyro.clock.ui.theme.resolveLabelColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmControlsTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun switchingAnAlarmDoesNotMutateTheDisplayedSnapshot() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val alarm = Alarm(time = 0, enabled = true)
        var update: Alarm? = null
        try {
            controller.get().setContent {
                MaterialTheme {
                    AlarmItem(alarm = alarm, currentTime = System.currentTimeMillis(),
                        onClick = {}, onLongClick = {}, onUpdateAlarm = { update = it },
                        onDeleteAlarm = {}, onDismissAlarm = {})
                }
            }
            compose.onNode(isToggleable()).performClick()
            compose.runOnIdle {
                assertTrue(alarm.enabled)
                assertFalse(requireNotNull(update).enabled)
                assertNotSame(alarm, update)
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun defaultColorFollowsTheAppThemeWhileExplicitColorsStayFixed() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val dark = mutableStateOf(false)
        var default = Color.Unspecified
        var snow = Color.Unspecified
        var custom = Color.Unspecified
        try {
            controller.get().setContent {
                ClockYouTheme(darkTheme = dark.value, customColorScheme = lightColorScheme(), dynamicColor = false) {
                    default = resolveLabelColor(DefaultLabelColor)
                    snow = resolveLabelColor(LabelColor.Snow.argb)
                    custom = resolveLabelColor(0xFF123456.toInt())
                }
            }
            compose.runOnIdle {
                assertEquals(Color(LabelColor.Charcoal.argb), default)
                assertEquals(Color.White, snow)
                assertEquals(Color(0xFF123456.toInt()), custom)
                dark.value = true
            }
            compose.runOnIdle {
                assertEquals(Color(LabelColor.Snow.argb), default)
                assertEquals(Color.White, snow)
                assertEquals(Color(0xFF123456.toInt()), custom)
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }
}
