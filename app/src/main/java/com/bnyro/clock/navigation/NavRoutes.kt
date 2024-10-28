package com.bnyro.clock.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavRoutes(
    val route: String
) {
    data object Home : NavRoutes("home")
    data object Settings : NavRoutes("settings")
    data object AlarmPicker : NavRoutes("alarmPicker") {
        const val ALARM_ID = "alarmId"
        val routeWithArgs = "$route/{$ALARM_ID}"
        val args = listOf(navArgument(ALARM_ID) { NavType.LongType })
    }

    data object Permissions : NavRoutes("permissions")
}