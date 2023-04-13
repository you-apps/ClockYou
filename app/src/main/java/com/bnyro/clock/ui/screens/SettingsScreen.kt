package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bnyro.clock.R
import com.bnyro.clock.ui.prefs.CheckboxPref
import com.bnyro.clock.ui.prefs.SettingsCategory
import com.bnyro.clock.util.Preferences

@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        SettingsCategory(stringResource(R.string.appearance))
        CheckboxPref(
            prefKey = Preferences.showSecondsKey,
            title = stringResource(R.string.show_seconds),
            defaultValue = true
        )
    }
}
