package com.bnyro.clock

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.util.Preferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SettingsTabsTest {
    @Test
    fun lastTabStaysAccessibleAndEmptyPreferencesRecover() {
        Preferences.init(ApplicationProvider.getApplicationContext<Context>())
        Preferences.instance.edit().clear().commit()
        val model = SettingsModel()
        homeRoutes.forEach { model.toggleTab(it.route, false) }
        assertEquals(1, model.enabledTabs.size)
        homeRoutes.forEach { Preferences.instance.edit().putBoolean("show_tab_${it.route}", false).commit() }
        val recovered = SettingsModel()
        assertEquals(1, recovered.enabledTabs.size)
        assertTrue(Preferences.instance.getBoolean("show_tab_${recovered.enabledTabs.single()}", false))
    }
}
