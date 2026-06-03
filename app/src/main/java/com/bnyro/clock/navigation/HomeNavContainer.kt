package com.bnyro.clock.navigation

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.screens.alarm.AlarmScreen
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.presentation.screens.clock.ClockScreen
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.stopwatch.StopwatchScreen
import com.bnyro.clock.presentation.screens.stopwatch.model.StopwatchModel
import com.bnyro.clock.presentation.screens.timer.TimerScreen
import com.bnyro.clock.presentation.screens.timer.model.TimerModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeNavContainer(
    onNavigate: (route: String) -> Unit,
    initialTab: HomeRoutes,
    clockModel: ClockModel,
    timerModel: TimerModel,
    stopwatchModel: StopwatchModel,
    alarmModel: AlarmModel,
    settingsModel: SettingsModel
) {
    val orientation = LocalConfiguration.current.orientation
    val coroutineScope = rememberCoroutineScope()

    val filteredRoutes = remember(settingsModel.enabledTabs) {
        homeRoutes.filter { it.route in settingsModel.enabledTabs }
    }


    val initialPageIndex = remember(filteredRoutes) {
        val index = filteredRoutes.indexOfFirst { it.route == initialTab.route }
        if (index != -1) index else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPageIndex
    ) { filteredRoutes.size }

    Scaffold(
        bottomBar = {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                NavigationBar(tonalElevation = 5.dp) {
                    filteredRoutes.forEachIndexed { index, item ->
                        NavigationBarItem(
                            label = { Text(stringResource(item.stringRes)) },
                            icon = { Icon(item.icon, null) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            })
                    }
                }
            }
        }) { pV ->
        Row(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(pV)
                .padding(pV)
        ) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                NavigationRail {
                    filteredRoutes.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = { Icon(item.icon, null) },
                            label = { Text(stringResource(item.stringRes)) })
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                if (pageIndex < filteredRoutes.size) {
                    when (filteredRoutes[pageIndex]) {
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
        }
    }
}