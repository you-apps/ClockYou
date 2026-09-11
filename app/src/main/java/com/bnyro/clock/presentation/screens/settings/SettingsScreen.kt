package com.bnyro.clock.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.domain.model.TimerPickerBehaviour
import com.bnyro.clock.domain.model.VolumeButtonAction
import com.bnyro.clock.domain.model.WeekStart
import com.bnyro.clock.navigation.NavRoutes
import com.bnyro.clock.navigation.homeRoutes
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.ScrollPickerDialog
import com.bnyro.clock.presentation.screens.settings.components.ButtonGroupPref
import com.bnyro.clock.presentation.screens.settings.components.ColorPref
import com.bnyro.clock.presentation.screens.settings.components.IconPreference
import com.bnyro.clock.presentation.screens.settings.components.SettingsCategory
import com.bnyro.clock.presentation.screens.settings.components.SwitchPref
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.services.AlarmService
import com.bnyro.clock.util.services.TimerService

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onClickBack: () -> Unit,
    onNavigate: (String) -> Unit,
    settingsModel: SettingsModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    var showAlarmTimeoutDialog by remember { mutableStateOf(false) }
    var showTimerTimeoutDialog by remember { mutableStateOf(false) }
    var showTimerIncrementDialog by remember { mutableStateOf(false) }
    var showAlarmVolumeRampDialog by remember { mutableStateOf(false) }
    var showTimerVolumeRampDialog by remember { mutableStateOf(false) }
    var timerIncrementSeconds by remember {
        mutableIntStateOf(
            Preferences.instance.getInt(Preferences.timerIncrementSecondsKey, 60)
        )
    }
    var alarmTimeoutMinutes by remember {
        mutableIntStateOf(
            Preferences.instance.getInt(
                Preferences.alarmTimeoutMinutesKey,
                AlarmService.ALARM_TIMEOUT_MINUTES
            )
        )
    }
    var timerTimeoutMinutes by remember {
        mutableIntStateOf(
            Preferences.instance.getInt(
                Preferences.timerTimeoutMinutesKey,
                TimerService.TIMER_TIMEOUT_MINUTES
            )
        )
    }
    var alarmVolumeRampSeconds by remember {
        mutableIntStateOf(
            Preferences.instance.getInt(Preferences.alarmVolumeRampSecondsKey, 0)
        )
    }
    var timerVolumeRampSeconds by remember {
        mutableIntStateOf(
            Preferences.instance.getInt(Preferences.timerVolumeRampSecondsKey, 0)
        )
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { fileUri ->
            fileUri?.let { settingsModel.importAlarmsFromFosssify(context, it) }
        }
    )

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { fileUri ->
            fileUri?.let { settingsModel.exportAlarms(context, it) }
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    ClickableIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack) {
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
            SettingsCategory(stringResource(R.string.general))

            ButtonGroupPref(
                title = stringResource(R.string.theme),
                options = SettingsModel.Theme.entries.map { stringResource(it.resId) },
                values = SettingsModel.Theme.entries,
                currentValue = settingsModel.themeMode
            ) {
                settingsModel.themeMode = it
                Preferences.edit { putString(Preferences.themeKey, it.name) }
            }

            ButtonGroupPref(
                title = stringResource(R.string.color_scheme),
                options = SettingsModel.ColorTheme.entries.map { stringResource(it.resId) },
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


            ButtonGroupPref(
                title = "Name",
                options = SettingsModel.AppName.entries.map {
                    if (it == SettingsModel.AppName.DEFAULT) "Clock You" else "Clock"
                },
                values = SettingsModel.AppName.entries,
                currentValue = settingsModel.appName
            ) { selectedName ->
                settingsModel.updateAppName(context, selectedName)
            }

            ButtonGroupPref(
                title = stringResource(R.string.plus_button_position),
                options = SettingsModel.FabAlignment.entries.map {
                    it.name.lowercase().replaceFirstChar { char -> char.uppercase() }
                },
                values = SettingsModel.FabAlignment.entries,
                currentValue = settingsModel.fabAlignment
            ) { alignment ->
                settingsModel.updateFabAlignment(alignment)
            }

            ButtonGroupPref(
                title = stringResource(R.string.start_tab),
                options = homeRoutes.map { stringResource(it.stringRes) },
                values = homeRoutes,
                currentValue = settingsModel.homeTab
            ) {
                settingsModel.homeTab = it
                Preferences.edit { putString(Preferences.startTabKey, it.route) }
            }

            val tabItems = listOf(
                "alarm" to R.string.alarm,
                "clock" to R.string.clock,
                "timer" to R.string.timer,
                "stopwatch" to R.string.stopwatch
            )
            ButtonGroupPref(
                title = stringResource(R.string.show_clock_bottom_tab),
                options = tabItems.map { stringResource(it.second) },
                values = tabItems.map { it.first },
                currentValue = settingsModel.enabledTabs
            ) { selectedKey ->
                val key = selectedKey as String
                val currentState = Preferences.instance.getBoolean("show_tab_$key", true)
                settingsModel.toggleTab(key, !currentState)
            }

            ButtonGroupPref(
                title = stringResource(R.string.start_week_on),
                options = WeekStart.entries.map { stringResource(it.resId) },
                values = WeekStart.entries,
                currentValue = settingsModel.weekStart
            ) {
                settingsModel.weekStart = it
                Preferences.edit { putString(Preferences.weekStartKey, it.name) }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            SettingsCategory(stringResource(R.string.alarm))

            ButtonGroupPref(
                title = stringResource(R.string.alarm_picker_style),
                options = PickerStyle.entries.map {
                    stringResource(
                        when (it) {
                            PickerStyle.WHEEL -> R.string.scroll
                            PickerStyle.NUMBER_PAD -> R.string.number_pad
                            PickerStyle.CLOCK -> R.string.clock
                        }
                    )
                },
                values = PickerStyle.entries,
                currentValue = settingsModel.alarmPickerStyle
            ) {
                settingsModel.alarmPickerStyle = it
                Preferences.edit { putString(Preferences.alarmPickerStyleKey, it.name) }
            }

            ButtonGroupPref(
                title = stringResource(R.string.volume_buttons_during_alarm),
                options = listOf(
                    stringResource(R.string.snooze),
                    stringResource(R.string.dismiss),
                    stringResource(R.string.control_volume),
                    stringResource(R.string.do_nothing)
                ),
                values = VolumeButtonAction.entries,
                currentValue = settingsModel.volumeButtonAction
            ) { action ->
                settingsModel.volumeButtonAction = action
                Preferences.edit {
                    putString(Preferences.volumeButtonActionKey, action.name)
                }
            }

            IconPreference(
                title = stringResource(R.string.timeout_after),
                summary = pluralStringResource(
                    R.plurals.minutes,
                    alarmTimeoutMinutes,
                    alarmTimeoutMinutes
                ),
                imageVector = Icons.Rounded.Timer
            ) {
                showAlarmTimeoutDialog = true
            }

            IconPreference(
                title = stringResource(R.string.volume_ramp),
                summary = volumeRampSummary(alarmVolumeRampSeconds),
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp
            ) {
                showAlarmVolumeRampDialog = true
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            SettingsCategory(stringResource(R.string.clock))

            SwitchPref(
                prefKey = Preferences.showSecondsKey,
                title = stringResource(R.string.show_seconds),
                defaultValue = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            SettingsCategory(stringResource(R.string.timer))

            ButtonGroupPref(
                title = stringResource(R.string.timer_picker_style),
                options = PickerStyle.entries.map {
                    stringResource(
                        when (it) {
                            PickerStyle.WHEEL -> R.string.scroll
                            PickerStyle.NUMBER_PAD -> R.string.number_pad
                            PickerStyle.CLOCK -> R.string.clock
                        }
                    )
                },
                values = PickerStyle.entries,
                currentValue = settingsModel.timerPickerStyle
            ) {
                settingsModel.timerPickerStyle = it
                Preferences.edit { putString(Preferences.timerPickerStyleKey, it.name) }
            }

            ButtonGroupPref(
                title = stringResource(R.string.volume_buttons_during_timer),
                options = listOf(
                    stringResource(R.string.snooze),
                    stringResource(R.string.dismiss),
                    stringResource(R.string.control_volume),
                    stringResource(R.string.do_nothing)
                ),
                values = VolumeButtonAction.entries,
                currentValue = settingsModel.timerVolumeButtonAction
            ) { action ->
                settingsModel.timerVolumeButtonAction = action
                Preferences.edit {
                    putString(Preferences.timerVolumeButtonActionKey, action.name)
                }
            }

            ButtonGroupPref(
                title = stringResource(R.string.timer_picker_behaviour),
                options = TimerPickerBehaviour.entries.map {
                    stringResource(
                        when (it) {
                            TimerPickerBehaviour.HIDE -> R.string.picker_hide
                            TimerPickerBehaviour.KEEP_OPEN -> R.string.picker_keep_open
                        }
                    )
                },
                values = TimerPickerBehaviour.entries,
                currentValue = settingsModel.timerPickerBehaviour
            ) {
                settingsModel.timerPickerBehaviour = it
                Preferences.edit { putString(Preferences.timerPickerBehaviourKey, it.name) }
            }

            SwitchPref(
                prefKey = Preferences.timerBigStartButtonKey,
                title = stringResource(R.string.timer_use_big_start),
                defaultValue = false
            ) {
                settingsModel.timerBigStartButton = it
            }

            SwitchPref(
                prefKey = Preferences.timerFullScreenAlertKey,
                title = stringResource(R.string.timer_full_screen_alert),
                defaultValue = true
            )

            IconPreference(
                title = stringResource(R.string.timer_increment),
                summary = pluralStringResource(
                    R.plurals.seconds_count,
                    timerIncrementSeconds,
                    timerIncrementSeconds
                ),
                imageVector = Icons.Rounded.MoreTime
            ) {
                showTimerIncrementDialog = true
            }

            IconPreference(
                title = stringResource(R.string.timeout_after),
                summary = pluralStringResource(
                    R.plurals.minutes,
                    timerTimeoutMinutes,
                    timerTimeoutMinutes
                ),
                imageVector = Icons.Rounded.Timer
            ) {
                showTimerTimeoutDialog = true
            }

            IconPreference(
                title = stringResource(R.string.volume_ramp),
                summary = volumeRampSummary(timerVolumeRampSeconds),
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp
            ) {
                showTimerVolumeRampDialog = true
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )


            SettingsCategory(stringResource(R.string.widgets))

            IconPreference(
                title = stringResource(R.string.widgets_summary),

                imageVector = Icons.Rounded.Widgets
            ) {
                onNavigate(NavRoutes.Widgets.route)
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            SettingsCategory(stringResource(R.string.migrate_title))
            IconPreference(
                title = stringResource(R.string.Import_Alarms),
                summary = stringResource(R.string.importdescr),
                imageVector = Icons.Default.Restore
            ) {
                documentPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            }
            IconPreference(
                title = stringResource(R.string.export),
                summary = stringResource(R.string.exportdesc),
                imageVector = Icons.Default.Backup
            ) {
                exportDocumentLauncher.launch("clockyou_export.json")
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(stringResource(R.string.about))
            IconPreference(
                title = stringResource(R.string.source_code),
                summary = stringResource(R.string.source_code_summary),
                imageVector = Icons.AutoMirrored.Filled.OpenInNew
            ) {
                uriHandler.openUri("https://github.com/you-apps/ClockYou")
            }
            IconPreference(
                title = stringResource(R.string.clock_you_version), summary = stringResource(
                    R.string.version_value, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
                ), imageVector = Icons.Default.History
            ) {
                uriHandler.openUri("https://github.com/you-apps/ClockYou/releases/latest")
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(stringResource(R.string.credits))
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
    if (showTimerIncrementDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showTimerIncrementDialog = false },
            title = stringResource(R.string.select_timer_increment),
            unit = stringResource(R.string.seconds),
            value = timerIncrementSeconds,
            maxValue = 60,
            offset = 1,
            label = { it.toString() },
            onValueSet = {
                timerIncrementSeconds = it
                Preferences.edit { putInt(Preferences.timerIncrementSecondsKey, it) }
                showTimerIncrementDialog = false
            }
        )
    }
    if (showAlarmTimeoutDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showAlarmTimeoutDialog = false },
            title = stringResource(R.string.select_alarm_timeout),
            unit = stringResource(R.string.minutes),
            value = alarmTimeoutMinutes,
            maxValue = 120,
            offset = 1,
            label = { it.toString() },
            onValueSet = {
                alarmTimeoutMinutes = it
                Preferences.edit { putInt(Preferences.alarmTimeoutMinutesKey, it) }
                showAlarmTimeoutDialog = false
            }
        )
    }
    if (showTimerTimeoutDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showTimerTimeoutDialog = false },
            title = stringResource(R.string.select_timer_timeout),
            unit = stringResource(R.string.minutes),
            value = timerTimeoutMinutes,
            maxValue = 120,
            offset = 1,
            label = { it.toString() },
            onValueSet = {
                timerTimeoutMinutes = it
                Preferences.edit { putInt(Preferences.timerTimeoutMinutesKey, it) }
                showTimerTimeoutDialog = false
            }
        )
    }
    if (showAlarmVolumeRampDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showAlarmVolumeRampDialog = false },
            title = stringResource(R.string.select_volume_ramp),
            unit = stringResource(R.string.seconds),
            value = alarmVolumeRampSeconds,
            maxValue = 61,
            offset = 0,
            label = { it.toString() },
            onValueSet = {
                alarmVolumeRampSeconds = it
                Preferences.edit { putInt(Preferences.alarmVolumeRampSecondsKey, it) }
                showAlarmVolumeRampDialog = false
            }
        )
    }
    if (showTimerVolumeRampDialog) {
        ScrollPickerDialog(
            onDismissRequest = { showTimerVolumeRampDialog = false },
            title = stringResource(R.string.select_volume_ramp),
            unit = stringResource(R.string.seconds),
            value = timerVolumeRampSeconds,
            maxValue = 61,
            offset = 0,
            label = { it.toString() },
            onValueSet = {
                timerVolumeRampSeconds = it
                Preferences.edit { putInt(Preferences.timerVolumeRampSecondsKey, it) }
                showTimerVolumeRampDialog = false
            }
        )
    }
}

/**
 * A rise of no seconds at all is the sound arriving at once, which reads as never rather than as
 * zero seconds of rising.
 */
@Composable
private fun volumeRampSummary(seconds: Int) = if (seconds == 0) {
    stringResource(R.string.volume_ramp_never)
} else {
    pluralStringResource(R.plurals.seconds_count, seconds, seconds)
}
