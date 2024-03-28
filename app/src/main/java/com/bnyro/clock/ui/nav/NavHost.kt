package com.bnyro.clock.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.ui.model.AlarmModel
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.model.StopwatchModel
import com.bnyro.clock.ui.model.TimerModel
import com.bnyro.clock.ui.screens.AlarmScreen
import com.bnyro.clock.ui.screens.ClockScreen
import com.bnyro.clock.ui.screens.SettingsScreen
import com.bnyro.clock.ui.screens.StopwatchScreen
import com.bnyro.clock.ui.screens.TimerScreen

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
