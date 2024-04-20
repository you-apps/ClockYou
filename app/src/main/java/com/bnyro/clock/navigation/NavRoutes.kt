package com.bnyro.clock.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavRoutes(
    val route: String
) {
    object Home : NavRoutes("home")
    object Settings : NavRoutes("settings")
    object AlarmPicker : NavRoutes("alarmPicker") {
        const val alarmId = "alarmId"
        val routeWithArgs = "$route/{$alarmId}"
        val args = listOf(navArgument(alarmId) { NavType.LongType })
    }
}