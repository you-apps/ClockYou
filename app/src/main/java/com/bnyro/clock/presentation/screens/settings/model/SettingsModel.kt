package com.bnyro.clock.presentation.screens.settings.model

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.R
import com.bnyro.clock.navigation.HomeRoutes
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.catpucchinLatte
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.App
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

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

    fun importAlarmsFromFosssify(context: Context, uri: Uri) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).use { reader ->
                            reader.readText()
                        }
                    } ?: return@withContext false

                    val jsonObject = JSONObject(content)
                    if (!jsonObject.has("alarms")) return@withContext false

                    val alarmsArray = jsonObject.getJSONArray("alarms")

                    val appContainer = (context.applicationContext as App).container
                    val alarmRepository = appContainer.alarmRepository
                    val createUpdateDeleteAlarmUseCase = CreateUpdateDeleteAlarmUseCase(context.applicationContext, alarmRepository)

                    for (i in 0 until alarmsArray.length()) {
                        val item = alarmsArray.getJSONObject(i)

                        val rawTime = if (item.has("time")) {
                            item.getLong("time")
                        } else {
                            item.optLong("timeInMinutes", 0L)
                        }

                        val finalAlarmTime = if (rawTime <= 1440L) {
                            rawTime * 60 * 1000
                        } else {
                            rawTime
                        }

                        val daysMask = item.optInt("days", 0)
                        val parsedDaysList = mutableListOf<Int>()

                        for (dayIndex in 0..6) {
                            if ((daysMask and (1 shl dayIndex)) != 0) {

                                val correctDay = if (dayIndex == 6) 0 else dayIndex + 1
                                parsedDaysList.add(correctDay)
                            }
                        }

                        val isRepeatingAlarm = parsedDaysList.isNotEmpty()

                        val newAlarm = Alarm(
                            id = 0,
                            time = finalAlarmTime,
                            days = parsedDaysList,
                            enabled = item.optBoolean("isEnabled", false) || item.optBoolean("enabled", false),
                            vibrate = item.optBoolean("vibrate", false),
                            soundUri = item.optString("soundUri", null),
                            label = item.optString("label", ""),
                            repeat = isRepeatingAlarm
                        )

                        createUpdateDeleteAlarmUseCase.createAlarm(newAlarm)

                    }
                    true
                } catch (e: Exception) {
                    Log.e("SettingsModel", "error error we got a error D:D:D:D:D:D:D:", e)
                    false
                }
            }

            val message = if (success) "Alarms imported successfully!" else "Failed to import D:"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    var homeTab by mutableStateOf(
        homeRoutes.first {
            it.route == Preferences.instance.getString(
                Preferences.startTabKey, HomeRoutes.Alarm.route
            )
        })
}