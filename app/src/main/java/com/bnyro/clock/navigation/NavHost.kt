package com.bnyro.clock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.presentation.screens.alarm.AlarmScreen
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.clock.ClockScreen
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.SettingsScreen
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.stopwatch.StopwatchScreen
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.TimerScreen
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsModel: SettingsModel,
    clockModel: ClockModel,
    startDestination: NavRoutes,
    modifier: Modifier = Modifier
) {
    val alarmModel: AlarmModel = viewModel()
    val timerModel: TimerModel = viewModel()
    val stopwatchModel: StopwatchModel = viewModel()

    NavHost(navController, startDestination = startDestination.route, modifier = modifier) {
        composable(NavRoutes.Alarm.route) {
            AlarmScreen(onClickSettings = {
                navController.navigate(NavRoutes.Settings.route)
            }, alarmModel)
        }
        composable(NavRoutes.Clock.route) {
            ClockScreen(onClickSettings = {
                navController.navigate(NavRoutes.Settings.route)
            }, clockModel)
        }
        composable(NavRoutes.Timer.route) {
            TimerScreen(onClickSettings = {
                navController.navigate(NavRoutes.Settings.route)
            }, timerModel)
        }
        composable(NavRoutes.Stopwatch.route) {
            StopwatchScreen(onClickSettings = {
                navController.navigate(NavRoutes.Settings.route)
            }, stopwatchModel)
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(onClickBack = {
                navController.popBackStack()
            }, settingsModel, timerModel)
        }
    }
}
