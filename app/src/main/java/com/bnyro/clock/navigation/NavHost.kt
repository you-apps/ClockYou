package com.bnyro.clock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.SettingsScreen
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsModel: SettingsModel,
    initialTab: HomeRoutes,
    modifier: Modifier = Modifier
) {
    val alarmModel: AlarmModel = viewModel()
    val timerModel: TimerModel = viewModel()
    val stopwatchModel: StopwatchModel = viewModel()
    val clockModel: ClockModel = viewModel(factory = ClockModel.Factory)

    NavHost(navController, startDestination = NavRoutes.Home.route, modifier = modifier) {
        composable(NavRoutes.Home.route) {
            HomeNavContainer(
                onNavigate = {
                    navController.navigate(it.route)
                },
                alarmModel = alarmModel,
                clockModel = clockModel,
                timerModel = timerModel,
                stopwatchModel = stopwatchModel,
                initialTab = initialTab
            )
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(onClickBack = {
                navController.popBackStack()
            }, settingsModel, timerModel)
        }
    }
}