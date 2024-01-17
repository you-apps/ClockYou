package com.bnyro.clock.ui.nav

import android.util.Log
import androidx.activity.addCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.model.SettingsModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val bottomNavItems = listOf(
    NavRoutes.Alarm,
    NavRoutes.Clock,
    NavRoutes.Timer,
    NavRoutes.Stopwatch
)
val navRoutes = bottomNavItems + NavRoutes.Settings
@Composable
fun NavContainer(
    settingsModel: SettingsModel,
    initialTab: NavRoutes
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val clockModel: ClockModel = viewModel(factory = ClockModel.Factory)
    val navController = rememberNavController()

    var selectedRoute by remember {
        mutableStateOf(initialTab)
    }
    LaunchedEffect(Unit) {
        val activity = context as MainActivity
        activity.onBackPressedDispatcher.addCallback {
            if (selectedRoute != NavRoutes.Settings) {
                activity.finish()
            } else {
                navController.popBackStack()
            }
        }
    }

    // listen for destination changes (e.g. back presses)
    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            navRoutes.firstOrNull { it.route == destination.route }
                ?.let { selectedRoute = it }
        }

        // this needs to be launched in a new scope to avoid crashes when it's called too early
        // while the navController doesn't yet have the ability to navigate because the NavContainer
        // is not yet composed
        scope.launch {
            navController.navigate(selectedRoute.route)
            navController.addOnDestinationChangedListener(listener)
        }

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(
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
        AppNavHost(
            navController,
            settingsModel,
            clockModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(pV)
        )
    }
}
