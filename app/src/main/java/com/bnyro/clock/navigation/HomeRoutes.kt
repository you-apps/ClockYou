package com.bnyro.clock.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.bnyro.clock.R

sealed class HomeRoutes(
    val route: String,
    @StringRes val stringRes: Int,
    val icon: ImageVector
) {
    object Alarm : HomeRoutes("alarm", R.string.alarm, Icons.Default.Alarm)
    object Clock : HomeRoutes("clock", R.string.clock, Icons.Default.Schedule)
    object Timer : HomeRoutes("timer", R.string.timer, Icons.Default.AvTimer)
    object Stopwatch : HomeRoutes("stopwatch", R.string.stopwatch, Icons.Outlined.Timer)
}

val homeRoutes = listOf(
    HomeRoutes.Alarm, HomeRoutes.Clock, HomeRoutes.Timer, HomeRoutes.Stopwatch
)