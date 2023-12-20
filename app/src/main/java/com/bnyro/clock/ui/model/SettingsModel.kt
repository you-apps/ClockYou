package com.bnyro.clock.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.R
import com.bnyro.clock.util.Preferences

class SettingsModel : ViewModel() {
    enum class Theme(@StringRes val resId: Int) {
        SYSTEM(R.string.system), LIGHT(R.string.light), DARK(R.string.dark), AMOLED(R.string.amoled)
    }

    private val key =
        Preferences.instance.getString(Preferences.themeKey, Theme.SYSTEM.name) ?: Theme.SYSTEM.name

    var themeMode: Theme by mutableStateOf(
        Theme.valueOf(key.uppercase())
    )
}
