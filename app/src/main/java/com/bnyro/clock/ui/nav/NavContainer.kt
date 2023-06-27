package com.bnyro.clock.ui.nav

import androidx.activity.addCallback
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavContainer(
    settingsModel: SettingsModel,
    initialTab: NavRoutes
) {
    val context = LocalContext.current

    val clockModel: ClockModel = viewModel()
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        NavRoutes.Clock,
        NavRoutes.Alarm,
        NavRoutes.Timer,
        NavRoutes.Stopwatch
    )
    val navRoutes = bottomNavItems + NavRoutes.Settings

    var selectedRoute by remember {
        mutableStateOf(initialTab)
    }
    LaunchedEffect(Unit) {
        val activity = context as MainActivity
        activity.onBackPressedDispatcher.addCallback {
            if (selectedRoute != NavRoutes.Settings) activity.finish()
            else navController.popBackStack()
        }
    }

    // listen for destination changes (e.g. back presses)
    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            navRoutes.firstOrNull { it.route == destination.route }
                ?.let { selectedRoute = it }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = when (selectedRoute) {
            NavRoutes.Settings ->
                Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)

            else -> Modifier
        },
        topBar = {
            Crossfade(selectedRoute) { navRoute ->
                when (navRoute) {
                    NavRoutes.Settings -> LargeTopAppBar(
                        title = {
                            Text(stringResource(selectedRoute.stringRes))
                        },
                        navigationIcon = {
                            ClickableIcon(imageVector = Icons.Default.ArrowBack) {
                                navController.popBackStack()
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )

                    else -> TopAppBar(
                        title = {
                            Text(stringResource(selectedRoute.stringRes))
                        },
                        actions = {
                            if (selectedRoute == NavRoutes.Clock) {
                                Box {
                                    var showDropdown by remember {
                                        mutableStateOf(false)
                                    }

                                    ClickableIcon(imageVector = Icons.Default.Sort) {
                                        showDropdown = true
                                    }

                                    DropdownMenu(
                                        expanded = showDropdown,
                                        onDismissRequest = { showDropdown = false }
                                    ) {
                                        SortOrder.values().forEach {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(it.value))
                                                },
                                                onClick = {
                                                    clockModel.sortOrder = it
                                                    Preferences.edit {
                                                        putString(Preferences.clockSortOrder, it.name)
                                                    }
                                                    showDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            ClickableIcon(imageVector = Icons.Default.Settings) {
                                navController.navigate(NavRoutes.Settings.route)
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 5.dp
            ) {
                bottomNavItems.forEach {
                    NavigationBarItem(
                        label = {
                            Text(stringResource(it.stringRes))
                        },
                        icon = {
                            Icon(it.icon, null)
                        },
                        selected = it == selectedRoute,
                        onClick = {
                            selectedRoute = it
                            navController.navigate(it.route)
                        }
                    )
                }
            }
        }
    ) { pV ->
        Box(
            modifier = Modifier.padding(pV)
        ) {
            AppNavHost(navController, settingsModel, clockModel)
        }
    }
}
