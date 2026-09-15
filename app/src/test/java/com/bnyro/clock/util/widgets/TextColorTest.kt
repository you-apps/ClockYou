package com.bnyro.clock.util.widgets

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 35], application = Application::class)
class TextColorTest {
    @Test
    fun widgetAndPickerUseTheSamePaletteInBothThemes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (nightMode in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
            val configuration = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
            }
            val themedContext = context.createConfigurationContext(configuration)
            for (textColor in TextColor.entries.filter { it != TextColor.Custom }) {
                val views = RemoteViews(context.packageName, R.layout.digital_clock)
                views.applyTextColor(themedContext, R.id.textClock2, textColor)
                val rendered = views.apply(themedContext, null)
                    .findViewById<TextView>(R.id.textClock2)
                assertEquals(
                    textColor.getColorValue(themedContext),
                    rendered.currentTextColor
                )
            }
            assertEquals(
                themedContext.getColor(R.color.widget_color_primary_dark),
                TextColor.PrimaryDark.getColorValue(themedContext)
            )
        }
    }

    @Test
    fun customColorPreservesTransparencyAndDefaultsToWhite() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (customColor in listOf(0, 0x804488CC.toInt(), null)) {
            val views = RemoteViews(context.packageName, R.layout.digital_clock)
            views.applyTextColor(context, R.id.textClock2, TextColor.Custom, customColor)
            val rendered = views.apply(context, null).findViewById<TextView>(R.id.textClock2)
            val expected = customColor ?: context.getColor(android.R.color.white)
            assertEquals(expected, TextColor.Custom.getColorValue(context, customColor))
            assertEquals(expected, rendered.currentTextColor)
        }
    }
}
