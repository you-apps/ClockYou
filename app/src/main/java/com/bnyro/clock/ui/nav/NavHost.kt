package com.bnyro.clock.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.ui.model.AlarmModel
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.model.StopwatchModel
import com.bnyro.clock.ui.model.TimerModel
import com.bnyro.clock.ui.screens.AlarmScreen
import com.bnyro.clock.ui.screens.ClockScreen
import com.bnyro.clock.ui.screens.StopwatchScreen
import com.bnyro.clock.ui.screens.TimerScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    val clockModel: ClockModel = viewModel()
    val alarmModel: AlarmModel = viewModel()
    val timerModel: TimerModel = viewModel()
    val stopwatchModel: StopwatchModel = viewModel()

    NavHost(navController, startDestination = NavRoutes.Clock.id) {
        composable(NavRoutes.Clock.id) {
            ClockScreen(clockModel)
        }
        composable(NavRoutes.Alarm.id) {
            AlarmScreen(alarmModel)
        }
        composable(NavRoutes.Timer.id) {
            TimerScreen(timerModel)
        }
        composable(NavRoutes.Stopwatch.id) {
            StopwatchScreen(stopwatchModel)
        }
    }
}
