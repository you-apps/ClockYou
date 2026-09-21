package com.bnyro.clock

import com.bnyro.clock.domain.model.TimerDescriptor
import com.bnyro.clock.domain.model.TimerSettings
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerLabelColorTest {
    @Test
    fun savedTimerColorsSurviveSerializationAndStarting() {
        val settings = TimerSettings(seconds = 600, label = "Tea", labelColor = 0xFF123456.toInt())
        val restored = Json.decodeFromString<TimerSettings>(Json.encodeToString(settings))
        val running = TimerDescriptor(1, restored).asScheduledObject()

        assertEquals(settings.labelColor, running.labelColor.value)
        assertEquals(settings, running.settings)
    }

    @Test
    fun savedTimersWithoutAColorDefaultToSnow() {
        val settings = Json.decodeFromString<TimerSettings>("""{"seconds":600,"label":"Tea"}""")

        assertEquals(-1, settings.labelColor)
        assertEquals(-1, TimerDescriptor(1, settings).asScheduledObject().labelColor.value)
    }
}
