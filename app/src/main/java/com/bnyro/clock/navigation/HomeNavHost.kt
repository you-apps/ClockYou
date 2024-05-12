package com.bnyro.clock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.presentation.screens.alarm.AlarmScreen
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.clock.ClockScreen
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.stopwatch.StopwatchScreen
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.TimerScreen
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun HomeNavHost(
    navController: NavHostController,
    onNavigate: (route: String) -> Unit,
    startDestination: HomeRoutes,
    clockModel: ClockModel,
    alarmModel: AlarmModel,
    timerModel: TimerModel,
    stopwatchModel: StopwatchModel
) {
    NavHost(navController, startDestination = startDestination.route) {
        composable(HomeRoutes.Alarm.route) {
            AlarmScreen(onClickSettings = {
                onNavigate(NavRoutes.Settings.route)
            }, onAlarm = {
                onNavigate("${NavRoutes.AlarmPicker.route}/$it")
            }, alarmModel)
        }
        composable(HomeRoutes.Clock.route) {
            ClockScreen(onClickSettings = {
                onNavigate(NavRoutes.Settings.route)
            }, clockModel)
        }
        composable(HomeRoutes.Timer.route) {
            TimerScreen(onClickSettings = {
                onNavigate(NavRoutes.Settings.route)
            }, timerModel)
        }
        composable(HomeRoutes.Stopwatch.route) {
            StopwatchScreen(onClickSettings = {
                onNavigate(NavRoutes.Settings.route)
            }, stopwatchModel)
        }
    }
}