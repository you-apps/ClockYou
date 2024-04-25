package com.bnyro.clock.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.alarmpicker.AlarmPickerScreen
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.permission.PermissionScreen
import com.bnyro.clock.presentation.screens.settings.SettingsScreen
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsModel: SettingsModel,
    initialTab: HomeRoutes,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val alarmModel: AlarmModel = viewModel()
    val timerModel: TimerModel = viewModel()
    val stopwatchModel: StopwatchModel = viewModel()
    val clockModel: ClockModel = viewModel()

    NavHost(navController, startDestination = startDestination, modifier = modifier) {
        composable(NavRoutes.Home.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    initialOffset = { it / 4 }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            HomeNavContainer(
                onNavigate = {
                    navController.navigate(it)
                },
                alarmModel = alarmModel,
                clockModel = clockModel,
                timerModel = timerModel,
                stopwatchModel = stopwatchModel,
                initialTab = initialTab
            )
        }
        composable(NavRoutes.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            SettingsScreen(onClickBack = {
                navController.popBackStack()
            }, settingsModel, timerModel)
        }

        composable(NavRoutes.AlarmPicker.routeWithArgs, arguments = NavRoutes.AlarmPicker.args,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            AlarmPickerScreen {
                navController.popBackStack()
            }
        }

        composable(NavRoutes.Permissions.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            PermissionScreen {
                navController.navigate(NavRoutes.Home.route)
            }
        }
    }
}