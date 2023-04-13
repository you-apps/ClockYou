package com.bnyro.clock.ui.nav

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.model.SettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavContainer(
    settingsModel: SettingsModel
) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        NavRoutes.Clock,
        NavRoutes.Alarm,
        NavRoutes.Timer,
        NavRoutes.Stopwatch
    )
    val navRoutes = bottomNavItems + NavRoutes.Settings

    var selectedRoute by remember {
        mutableStateOf<NavRoutes>(NavRoutes.Clock)
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
            AppNavHost(navController, settingsModel)
        }
    }
}
