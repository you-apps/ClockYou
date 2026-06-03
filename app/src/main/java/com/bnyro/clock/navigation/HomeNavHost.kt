package com.bnyro.clock.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bnyro.clock.presentation.screens.alarm.AlarmScreen
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.clock.ClockScreen
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.stopwatch.StopwatchScreen
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.TimerScreen
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

val mainTabs = listOf(
    HomeRoutes.Alarm,
    HomeRoutes.Clock,
    HomeRoutes.Timer,
    HomeRoutes.Stopwatch
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePager(
    pagerState: PagerState,
    onNavigate: (route: String) -> Unit,
    clockModel: ClockModel, 
    alarmModel: AlarmModel,
    timerModel: TimerModel,
    stopwatchModel: StopwatchModel,
    settingsModel: SettingsModel
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        when (mainTabs[pageIndex]) {
            HomeRoutes.Alarm -> {
                AlarmScreen(
                    onClickSettings = { onNavigate(NavRoutes.Settings.route) },
                    onAlarm = { onNavigate("${NavRoutes.AlarmPicker.route}/$it") },
                    alarmModel = alarmModel,
                    settingsModel = settingsModel
                )
            }
            HomeRoutes.Clock -> {
                ClockScreen(
                    onClickSettings = { onNavigate(NavRoutes.Settings.route) },
                    clockModel = clockModel,
                    settingsModel = settingsModel
                )
            }
            HomeRoutes.Timer -> {
                TimerScreen(
                    onClickSettings = { onNavigate(NavRoutes.Settings.route) },
                    timerModel = timerModel,
                    settingsModel = settingsModel
                )
            }
            HomeRoutes.Stopwatch -> {
                StopwatchScreen(
                    onClickSettings = { onNavigate(NavRoutes.Settings.route) },
                    stopwatchModel = stopwatchModel
                )
            }
        }
    }
}