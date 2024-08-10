package com.bnyro.clock.presentation.screens.settings.model

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.R
import com.bnyro.clock.navigation.HomeRoutes
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.catpucchinLatte

class SettingsModel : ViewModel() {
    enum class Theme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), LIGHT(R.string.light), DARK(R.string.dark),
        AMOLED(R.string.amoled)
    }

    enum class ColorTheme(@StringRes val resId: Int) {
        SYSTEM(R.string.system),
        CATPPUCCIN(R.string.catppuccin)
    }

    private val themeModePref =
        Preferences.instance.getString(Preferences.themeKey, Theme.SYSTEM.name) ?: Theme.SYSTEM.name

    var themeMode: Theme by mutableStateOf(
        Theme.valueOf(themeModePref.uppercase())
    )
    private val colorThemePref =
        Preferences.instance.getString(Preferences.colorThemeKey, ColorTheme.SYSTEM.name)
            ?: ColorTheme.SYSTEM.name

    var colorTheme: ColorTheme by mutableStateOf(
        ColorTheme.valueOf(colorThemePref.uppercase())
    )

    var customColor by mutableStateOf(
        Preferences.instance.getInt(
            Preferences.customColorKey,
            catpucchinLatte.first()
        )
    )

    var homeTab by mutableStateOf(
        homeRoutes.first {
            it.route == Preferences.instance.getString(
                Preferences.startTabKey,
                HomeRoutes.Alarm.route
            )
        }
    )
}
