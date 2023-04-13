package com.bnyro.clock.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavContainer() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        NavRoutes.Clock,
        NavRoutes.Alarm,
        NavRoutes.Timer,
        NavRoutes.Stopwatch
    )

    var selectedRoute by remember {
        mutableStateOf<NavRoutes>(NavRoutes.Clock)
    }

    // listen for destination changes (e.g. back presses)
    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            bottomNavItems.firstOrNull { it.route == destination.route }?.let {
                selectedRoute = it
            }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(selectedRoute.stringRes))
                }
            )
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
            AppNavHost(navController)
        }
    }
}
