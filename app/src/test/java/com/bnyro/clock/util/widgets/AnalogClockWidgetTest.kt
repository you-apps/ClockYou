package com.bnyro.clock.util.widgets

import android.app.Application
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AnalogClockFace
import com.bnyro.clock.domain.model.AnalogClockWidgetOptions
import com.bnyro.clock.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 30, 35])
class AnalogClockWidgetTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun systemWidgetOpensTheAppAfterRepeatedUpdates() {
        val options = AnalogClockWidgetOptions("System", 0, 0, 0, 0)
        val views = RemoteViews(context.packageName, R.layout.analog_clock)
        views.applyAnalogClockWidgetOptions(7, options, context)
        val root = views.apply(context, FrameLayout(context))
        views.reapply(context, root)
        val container = root.findViewById<ViewGroup>(R.id.analog_clock_container)

        assertEquals(1, container.childCount)
        assertTrue(container.performClick())
        assertEquals(
            MainActivity::class.java.name,
            shadowOf(context as Application).nextStartedActivity.component?.className
        )
    }

    @Test
    @Config(sdk = [35])
    fun changingFromCustomToSystemFaceReplacesTheClock() {
        val face = AnalogClockFace.Classic76
        val options = AnalogClockWidgetOptions(face.name, face.hourHand, face.minuteHand, face.secondHand, face.dial)
        val custom = RemoteViews(context.packageName, R.layout.analog_clock)
        custom.applyAnalogClockWidgetOptions(7, options, context)
        val root = custom.apply(context, FrameLayout(context))
        val container = root.findViewById<ViewGroup>(R.id.analog_clock_container)
        val previousClock = container.getChildAt(0)
        val system = RemoteViews(context.packageName, R.layout.analog_clock)
        system.applyAnalogClockWidgetOptions(7, AnalogClockWidgetOptions("System", 0, 0, 0, 0), context)
        system.reapply(context, root)

        assertEquals(1, container.childCount)
        assertNotSame(previousClock, container.getChildAt(0))
        assertTrue(container.performClick())
    }
}
