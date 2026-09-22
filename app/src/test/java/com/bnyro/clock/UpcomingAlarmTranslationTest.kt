package com.bnyro.clock

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class UpcomingAlarmTranslationTest {
    @Test
    fun namedUpcomingAlarmFormatsWithOnlyTheLabelInEveryLocale() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val label = "Morning walk"
        for (languageTag in context.resources.assets.locales) {
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(languageTag))
            }
            val localizedContext = context.createConfigurationContext(configuration)
            val title = localizedContext.getString(R.string.upcoming_named_alarm, label)
            assertTrue(languageTag, title.contains(label))
        }
    }
}
