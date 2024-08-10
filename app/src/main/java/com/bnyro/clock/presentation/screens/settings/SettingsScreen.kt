package com.bnyro.clock.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.screens.settings.components.ButtonGroupPref
import com.bnyro.clock.presentation.screens.settings.components.ColorPref
import com.bnyro.clock.presentation.screens.settings.components.IconPreference
import com.bnyro.clock.presentation.screens.settings.components.SettingsCategory
import com.bnyro.clock.presentation.screens.settings.components.SwitchPref
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.timer.model.TimerModel
import com.bnyro.clock.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClickBack: () -> Unit,
    settingsModel: SettingsModel,
    timerModel: TimerModel
) {
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    ClickableIcon(imageVector = Icons.Default.ArrowBack) {
                        onClickBack.invoke()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(pv)
                .verticalScroll(scrollState)
        ) {
            val uriHandler = LocalUriHandler.current
            SettingsCategory(stringResource(R.string.appearance))
            ButtonGroupPref(
                title = stringResource(R.string.theme),
                options = SettingsModel.Theme.entries.map {
                    stringResource(it.resId)
                },
                values = SettingsModel.Theme.entries,
                currentValue = settingsModel.themeMode
            ) {
                settingsModel.themeMode = it
                Preferences.edit { putString(Preferences.themeKey, it.name) }
            }
            ButtonGroupPref(
                title = stringResource(R.string.color_scheme),
                options = SettingsModel.ColorTheme.entries.map {
                    stringResource(it.resId)
                },
                values = SettingsModel.ColorTheme.entries,
                currentValue = settingsModel.colorTheme
            ) {
                settingsModel.colorTheme = it
                Preferences.edit { putString(Preferences.colorThemeKey, it.name) }
            }
            AnimatedVisibility(
                visible = settingsModel.colorTheme == SettingsModel.ColorTheme.CATPPUCCIN
            ) {
                ColorPref(
                    selectedColor = settingsModel.customColor,
                    onSelect = {
                        settingsModel.customColor = it
                        Preferences.edit { putInt(Preferences.customColorKey, it) }
                    }
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(title = stringResource(R.string.behavior))
            ButtonGroupPref(
                title = stringResource(R.string.start_tab),
                options = homeRoutes.map { stringResource(it.stringRes) },
                values = homeRoutes,
                currentValue = settingsModel.homeTab
            ) {
                settingsModel.homeTab = it
                Preferences.edit { putString(Preferences.startTabKey, it.route) }
            }
            SwitchPref(
                prefKey = Preferences.showSecondsKey,
                title = stringResource(R.string.show_seconds),
                defaultValue = true
            )
            SwitchPref(
                prefKey = Preferences.timerUsePickerKey,
                title = stringResource(R.string.timer_use_time_picker),
                defaultValue = false
            ) {
                // reset the timer model state to prevent issues when changing the time picker layout
                timerModel.timePickerFakeUnits = 0
                timerModel.timePickerSeconds = 0
            }
            SwitchPref(
                prefKey = Preferences.timerShowExamplesKey,
                title = stringResource(R.string.show_timer_quick_selection),
                defaultValue = true
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(stringResource(R.string.about))
            IconPreference(
                title = stringResource(R.string.source_code),
                summary = stringResource(R.string.source_code_summary),
                imageVector = Icons.Default.OpenInNew
            ) {
                uriHandler.openUri("https://github.com/you-apps/ClockYou")
            }
            IconPreference(
                title = stringResource(R.string.app_name),
                summary = stringResource(
                    R.string.version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ),
                imageVector = Icons.Default.History
            ) {
                uriHandler.openUri("https://github.com/you-apps/ClockYou/releases/latest")
            }
        }
    }
}
