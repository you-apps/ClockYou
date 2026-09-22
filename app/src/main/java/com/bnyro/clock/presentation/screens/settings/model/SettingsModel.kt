package com.bnyro.clock.presentation.screens.settings.model

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.BackupTimer
import com.bnyro.clock.domain.usecase.ClockBackupUseCase
import com.bnyro.clock.util.ClockBackupArchive
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.domain.model.TimerPickerBehaviour
import com.bnyro.clock.domain.model.WeekStart
import com.bnyro.clock.navigation.HomeRoutes
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.catpucchinLatte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.GregorianCalendar
import com.bnyro.clock.domain.model.VolumeButtonAction

class SettingsModel : ViewModel() {
    enum class Theme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), LIGHT(R.string.light), DARK(R.string.dark), AMOLED(R.string.amoled)
    }

    enum class ColorTheme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), CATPPUCCIN(R.string.catppuccin)
    }

    var themeMode: Theme by mutableStateOf(Theme.SYSTEM)

    var colorTheme: ColorTheme by mutableStateOf(ColorTheme.SYSTEM)
    var timerPickerStyle by mutableStateOf(PickerStyle.WHEEL)
    var timerPickerBehaviour by mutableStateOf(TimerPickerBehaviour.HIDE)
    var timerBigStartButton by mutableStateOf(false)
    var alarmPickerStyle by mutableStateOf(PickerStyle.WHEEL)
    var weekStart by mutableStateOf(WeekStart.MONDAY)
    var customColor by mutableStateOf(catpucchinLatte.first())
    var enabledTabs by mutableStateOf(emptyList<String>())

    fun toggleTab(route: String, enabled: Boolean) {
        if (!enabled && enabledTabs.size == 1 && route in enabledTabs) return
        Preferences.edit { putBoolean("show_tab_$route", enabled) }
        val newList = homeRoutes.mapNotNull { r ->
            r.route.takeIf { Preferences.instance.getBoolean("show_tab_${r.route}", true) }
        }
        enabledTabs = newList
    }

    enum class FabAlignment(val position: FabPosition) {
        LEFT(FabPosition.Start), RIGHT(FabPosition.End)
    }

    var fabAlignment: FabAlignment by mutableStateOf(FabAlignment.RIGHT)
        private set

    var volumeButtonAction by mutableStateOf(VolumeButtonAction.SNOOZE)

    var timerVolumeButtonAction by mutableStateOf(VolumeButtonAction.DISMISS)

    fun updateFabAlignment(alignment: FabAlignment) {
        Preferences.edit { putString("fab_alignment", alignment.name) }
        fabAlignment = alignment
    }

    enum class AppName(@StringRes val resId: Int) {
        DEFAULT(R.string.app_name),
        ALTERNATIVE(R.string.altname)
    }
    var appName: AppName by mutableStateOf(AppName.DEFAULT)
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

    fun exportBackup(context: Context, uri: Uri, activeTimers: List<BackupTimer>) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val backup = ClockBackupUseCase(context).capture(activeTimers)
                    ClockBackupArchive.exportBackup(context, uri, backup)
                }.onFailure { Log.e("SettingsModel", "Unable to export backup", it) }.isSuccess
            }
            Toast.makeText(context, if (success) R.string.backup_exported else R.string.backup_export_failed, Toast.LENGTH_LONG).show()
        }
    }

    fun importBackup(context: Context, uri: Uri, onRestored: (List<BackupTimer>) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val backup = ClockBackupArchive.importBackup(context, uri)
                    ClockBackupUseCase(context).restore(backup)
                    backup.activeTimers
                }.onFailure { Log.e("SettingsModel", "Unable to import backup", it) }
            }
            Toast.makeText(context, if (result.isSuccess) R.string.backup_imported else R.string.backup_import_failed, Toast.LENGTH_LONG).show()
            result.onSuccess { timers ->
                updateAppName(context, AppName.valueOf(
                    Preferences.instance.getString("app_name_key", AppName.DEFAULT.name) ?: AppName.DEFAULT.name
                ))
                loadPreferences()
                onRestored(timers)
            }
        }
    }

    var homeTab: HomeRoutes by mutableStateOf(HomeRoutes.Alarm)

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        themeMode = Theme.valueOf(
            (Preferences.instance.getString(Preferences.themeKey, Theme.SYSTEM.name)
                ?: Theme.SYSTEM.name).uppercase()
        )
        colorTheme = ColorTheme.valueOf(
            (Preferences.instance.getString(Preferences.colorThemeKey, ColorTheme.SYSTEM.name)
                ?: ColorTheme.SYSTEM.name).uppercase()
        )
        timerPickerStyle = PickerStyle.valueOf(
            Preferences.instance.getString(
                Preferences.timerPickerStyleKey,
                PickerStyle.WHEEL.name
            ) ?: PickerStyle.WHEEL.name
        )
        timerPickerBehaviour = TimerPickerBehaviour.valueOf(
            Preferences.instance.getString(
                Preferences.timerPickerBehaviourKey,
                TimerPickerBehaviour.HIDE.name
            ) ?: TimerPickerBehaviour.HIDE.name
        )
        timerBigStartButton = Preferences.instance.getBoolean(Preferences.timerBigStartButtonKey, false)
        alarmPickerStyle = PickerStyle.valueOf(
            Preferences.instance.getString(
                Preferences.alarmPickerStyleKey,
                PickerStyle.WHEEL.name
            ) ?: PickerStyle.WHEEL.name
        )
        weekStart = Preferences.instance.getString(Preferences.weekStartKey, null)
            ?.let { WeekStart.valueOf(it) }
            ?: WeekStart.entries.first {
                it.dayOfWeek.value % 7 == GregorianCalendar().firstDayOfWeek - 1
            }
        customColor = Preferences.instance.getInt(Preferences.customColorKey, catpucchinLatte.first())
        enabledTabs = homeRoutes.mapNotNull { route ->
            route.route.takeIf { Preferences.instance.getBoolean("show_tab_${route.route}", true) }
        }.ifEmpty {
            Preferences.edit { putBoolean("show_tab_${HomeRoutes.Alarm.route}", true) }
            listOf(HomeRoutes.Alarm.route)
        }
        fabAlignment = FabAlignment.valueOf(
            (Preferences.instance.getString("fab_alignment", FabAlignment.RIGHT.name)
                ?: FabAlignment.RIGHT.name).uppercase()
        )
        volumeButtonAction = VolumeButtonAction.valueOf(
            Preferences.instance.getString(
                Preferences.volumeButtonActionKey,
                VolumeButtonAction.SNOOZE.name
            ) ?: VolumeButtonAction.SNOOZE.name
        )
        timerVolumeButtonAction = VolumeButtonAction.valueOf(
            Preferences.instance.getString(
                Preferences.timerVolumeButtonActionKey,
                VolumeButtonAction.DISMISS.name
            ) ?: VolumeButtonAction.DISMISS.name
        )
        appName = AppName.valueOf(
            (Preferences.instance.getString("app_name_key", AppName.DEFAULT.name)
                ?: AppName.DEFAULT.name).uppercase()
        )
        homeTab = homeRoutes.first {
            it.route == Preferences.instance.getString(
                Preferences.startTabKey, HomeRoutes.Alarm.route
            )
        }
    }
}
