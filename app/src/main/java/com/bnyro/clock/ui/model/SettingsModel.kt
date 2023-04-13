package com.bnyro.clock.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.util.Preferences

class SettingsModel : ViewModel() {
    var themeMode by mutableStateOf(
        Preferences.instance.getString(Preferences.themeKey, "system")
    )
}
