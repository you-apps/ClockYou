package com.bnyro.clock.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.navigation.homeRoutes
import kotlinx.serialization.json.Json

object Preferences {
    lateinit var instance: SharedPreferences

    const val showSecondsKey = "showSeconds"
    const val themeKey = "theme"
    const val timerPickerStyleKey = "timerUsePicker"
    const val alarmPickerStyleKey = "alarm_use_scroll_picker"
    const val clockSortOrder = "clockSortOrder"
    const val savedTimersKey = "savedTimers"
    const val timerBigStartButtonKey = "timerBigStartButton"
    const val timerIncrementSecondsKey = "timerIncrementSeconds"
    const val timerPickerBehaviourKey = "timerPickerBehaviour"
    const val timerFullScreenAlertKey = "timerFullScreenAlert"
    const val timerTimeoutMinutesKey = "timerTimeoutMinutes"
    const val timerVolumeRampSecondsKey = "timerVolumeRampSeconds"
    const val alarmVolumeRampSecondsKey = "alarmVolumeRampSeconds"
    const val timerVolumeButtonActionKey = "timerVolumeButtonAction"
    const val snoozeTimeMinutesKey = "snoozeTimeMinutes"
    const val alarmTimeoutMinutesKey = "alarmTimeoutMinutes"
    const val customColorKey = "customColor"
    const val colorThemeKey = "colorTheme"
    const val startTabKey = "startTab"
    const val weekStartKey = "weekStart"
    const val volumeButtonActionKey = "volumeButtonAction"


    fun init(context: Context) {
        instance = context.getSharedPreferences("clock_you", Context.MODE_PRIVATE)

        val timerPickerStyle = instance.all[timerPickerStyleKey]
        val alarmPickerStyle = instance.all[alarmPickerStyleKey]
        val savedTimers = instance.all[savedTimersKey]
        val persistentTimers = instance.all["persistentTimers"]
        val migratedBigStartButton = instance.all[timerBigStartButtonKey]
        val oldBigStartButton = instance.all["timer_BIG_start_button"]
        instance.edit {
            if (timerPickerStyle is Boolean) {
                putString(
                    timerPickerStyleKey,
                    if (timerPickerStyle) PickerStyle.NUMBER_PAD.name else PickerStyle.WHEEL.name
                )
            }
            if (alarmPickerStyle is Boolean) {
                putString(
                    alarmPickerStyleKey,
                    if (alarmPickerStyle) PickerStyle.NUMBER_PAD.name else PickerStyle.WHEEL.name
                )
            }
            if (savedTimers == null && persistentTimers is String) {
                putString(
                    savedTimersKey,
                    Json.encodeToString(
                        persistentTimers.split(",").mapNotNull { it.toIntOrNull() }
                            .map { TimerSettings(seconds = it) }
                    )
                )
                remove("persistentTimers")
            }
            if (migratedBigStartButton == null && oldBigStartButton is Boolean) {
                putBoolean(timerBigStartButtonKey, oldBigStartButton)
                remove("timer_BIG_start_button")
            }
        }
    }

    fun edit(action: SharedPreferences.Editor.() -> Unit) = instance.edit(true, action)
}
