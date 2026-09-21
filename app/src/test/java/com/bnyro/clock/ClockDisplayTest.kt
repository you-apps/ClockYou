package com.bnyro.clock

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.components.ScrollTimerPicker
import com.bnyro.clock.presentation.screens.alarm.components.ScrollAlarmTimePicker
import com.bnyro.clock.presentation.screens.stopwatch.StopwatchScreen
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ClockDisplayTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun stopwatchFitsHourTransitionsAndExtremeDurations() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val model = StopwatchModel()
        try {
            activity.get().setContent {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    Surface {
                        Box(Modifier.width(360.dp).height(640.dp)) {
                            StopwatchScreen({}, model)
                        }
                    }
                }
            }
            for ((name, elapsed, text) in listOf(
                Triple("one-second", 1810L, "0:01 81"),
                Triple("before-hour", 3_599_990L, "59:59 99"),
                Triple("one-hour", 3_600_000L, "1:00:00 00"),
                Triple("many-hours", 444_444_560L, "123:27:24 56"),
                Triple("huge-hours", 3_599_999_999_990L, "999999:59:59 99"),
                Triple("maximum", Long.MAX_VALUE, "2562047788015:12:55 80")
            )) {
                compose.runOnIdle { model.currentPosition = elapsed }
                val result = mutableListOf<TextLayoutResult>()
                compose.onNodeWithText(text).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(result) }
                assertFalse(result.single().hasVisualOverflow)
                System.getProperty("clock.preview.dir")?.let { directory ->
                    File(directory).mkdirs()
                    File(directory, "stopwatch-$name.png").outputStream().use {
                        compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
            }
        } finally {
            activity.pause().stop().destroy()
        }
    }

    @Test
    fun wheelPickersDisplayTheirSeparators() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        try {
            for (alarm in listOf(false, true)) {
                activity.get().setContent {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        Surface {
                            Box(Modifier.width(360.dp).height(360.dp)) {
                                if (alarm) ScrollAlarmTimePicker(15, 33, onHoursChanged = {}, onMinutesChanged = {})
                                else ScrollTimerPicker(60) {}
                            }
                        }
                    }
                }
                compose.waitForIdle()
                val separators = compose.onAllNodes(SemanticsMatcher.expectValue(
                    androidx.compose.ui.semantics.SemanticsProperties.Text,
                    listOf(androidx.compose.ui.text.AnnotatedString(":"))
                )).fetchSemanticsNodes()
                org.junit.Assert.assertEquals(if (alarm) 1 else 2, separators.size)
                separators.forEach {
                    org.junit.Assert.assertTrue(it.boundsInRoot.width > 0)
                    org.junit.Assert.assertTrue(it.boundsInRoot.left >= 0 && it.boundsInRoot.right <= 360)
                }
                System.getProperty("clock.preview.dir")?.let { directory ->
                    File(directory).mkdirs()
                    File(directory, if (alarm) "alarm-picker.png" else "timer-picker.png").outputStream().use {
                        compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
            }
        } finally {
            activity.pause().stop().destroy()
        }
    }
}
