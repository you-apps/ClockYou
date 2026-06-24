package com.bnyro.clock.presentation.screens.settings.model

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.R
import com.bnyro.clock.navigation.HomeRoutes
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.catpucchinLatte

class SettingsModel : ViewModel() {
    enum class Theme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), LIGHT(R.string.light), DARK(R.string.dark), AMOLED(R.string.amoled)
    }

    enum class ColorTheme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), CATPPUCCIN(R.string.catppuccin)
    }

    private val themeModePref =
        Preferences.instance.getString(Preferences.themeKey, Theme.SYSTEM.name) ?: Theme.SYSTEM.name

    var themeMode: Theme by mutableStateOf(Theme.valueOf(themeModePref.uppercase()))

    private val colorThemePref =
        Preferences.instance.getString(Preferences.colorThemeKey, ColorTheme.SYSTEM.name)
            ?: ColorTheme.SYSTEM.name

    var colorTheme: ColorTheme by mutableStateOf(ColorTheme.valueOf(colorThemePref.uppercase()))
    var customColor by mutableStateOf(
        Preferences.instance.getInt(Preferences.customColorKey, catpucchinLatte.first())
    )
    var enabledTabs by mutableStateOf(
        homeRoutes.mapNotNull { route ->
            route.route.takeIf { Preferences.instance.getBoolean("show_tab_${route.route}", true) }
        })

    fun toggleTab(route: String, enabled: Boolean) {
        Preferences.edit { putBoolean("show_tab_$route", enabled) }
        val newList = homeRoutes.mapNotNull { r ->
            r.route.takeIf { Preferences.instance.getBoolean("show_tab_${r.route}", true) }
        }
        enabledTabs = newList
    }

    enum class FabAlignment(val position: FabPosition) {
        LEFT(FabPosition.Start), RIGHT(FabPosition.End)
    }

    private val fabAlignmentPref =
        Preferences.instance.getString("fab_alignment", FabAlignment.RIGHT.name)
            ?: FabAlignment.RIGHT.name

    var fabAlignment: FabAlignment by mutableStateOf(FabAlignment.valueOf(fabAlignmentPref.uppercase()))
        private set

    fun updateFabAlignment(alignment: FabAlignment) {
        Preferences.edit { putString("fab_alignment", alignment.name) }
        fabAlignment = alignment
    }

    enum class AppName(@StringRes val resId: Int) {
        DEFAULT(R.string.app_name),
        ALTERNATIVE(R.string.altname)
    }
    private val appNamePref =
        Preferences.instance.getString("app_name_key", AppName.DEFAULT.name) ?: AppName.DEFAULT.name

    var appName: AppName by mutableStateOf(AppName.valueOf(appNamePref.uppercase()))
        private set

    fun updateAppName(context: Context, newName: AppName) {
        if (appName == newName) return // No change needed
        Preferences.edit { putString("app_name_key", newName.name) }
        appName = newName

        val pm = context.applicationContext.packageManager
        val packageName = context.packageName

        val defaultAlias = ComponentName(context, "$packageName.ui.MainActivityDefault")
        val alternativeAlias = ComponentName(context, "$packageName.ui.MainActivityAlternative")

        val enableState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        val disableState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        if (newName == AppName.ALTERNATIVE) {
            pm.setComponentEnabledSetting(alternativeAlias, enableState, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(defaultAlias, disableState, PackageManager.DONT_KILL_APP)
        } else {
            pm.setComponentEnabledSetting(defaultAlias, enableState, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(alternativeAlias, disableState, PackageManager.DONT_KILL_APP)
        }
    }



    var homeTab by mutableStateOf(
        homeRoutes.first {
            it.route == Preferences.instance.getString(
                Preferences.startTabKey, HomeRoutes.Alarm.route
            )
        })
}