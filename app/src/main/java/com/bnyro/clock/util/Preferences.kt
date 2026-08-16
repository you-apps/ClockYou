package com.bnyro.clock.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.bnyro.clock.domain.model.AlarmPickerStyle
import com.bnyro.clock.domain.model.TimerPickerStyle
import com.bnyro.clock.navigation.homeRoutes

object Preferences {
    lateinit var instance: SharedPreferences

    const val showSecondsKey = "showSeconds"
    const val themeKey = "theme"
    const val timerPickerStyleKey = "timerUsePicker"
    const val alarmPickerStyleKey = "alarm_use_scroll_picker"
    const val timerShowExamplesKey = "timerShowExamples"
    const val clockSortOrder = "clockSortOrder"
    const val persistentTimerKey = "persistentTimers"
    const val snoozeTimeMinutesKey = "snoozeTimeMinutes"
    const val alarmTimeoutMinutesKey = "alarmTimeoutMinutes"
    const val customColorKey = "customColor"
    const val colorThemeKey = "colorTheme"
    const val startTabKey = "startTab"
    const val volumeButtonActionKey = "volumeButtonAction"


    fun init(context: Context) {
        instance = context.getSharedPreferences("clock_you", Context.MODE_PRIVATE)

        val timerPickerStyle = instance.all[timerPickerStyleKey]
        val alarmPickerStyle = instance.all[alarmPickerStyleKey]
        instance.edit {
            if (timerPickerStyle is Boolean) {
                putString(
                    timerPickerStyleKey,
                    if (timerPickerStyle) TimerPickerStyle.NUMBER_PAD.name else TimerPickerStyle.WHEEL.name
                )
            } else if (timerPickerStyle is String && TimerPickerStyle.entries.none { it.name == timerPickerStyle }) {
                putString(timerPickerStyleKey, TimerPickerStyle.WHEEL.name)
            }
            if (alarmPickerStyle is Boolean) {
                putString(
                    alarmPickerStyleKey,
                    if (alarmPickerStyle) AlarmPickerStyle.NUMBER_PAD.name else AlarmPickerStyle.WHEEL.name
                )
            }
        }
    }

    fun edit(action: SharedPreferences.Editor.() -> Unit) = instance.edit(true, action)
}
