package com.bnyro.clock.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.AlarmClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.services.StopwatchService
import com.bnyro.clock.ui.dialog.AlarmReceiverDialog
import com.bnyro.clock.ui.dialog.TimerReceiverDialog
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.model.StopwatchModel
import com.bnyro.clock.ui.nav.NavContainer
import com.bnyro.clock.ui.nav.NavRoutes
import com.bnyro.clock.ui.nav.bottomNavItems
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.ThemeUtil

class MainActivity : ComponentActivity() {

    val stopwatchModel by viewModels<StopwatchModel>()
    private var initialTab: NavRoutes = NavRoutes.Alarm

    lateinit var stopwatchService: StopwatchService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = (service as StopwatchService.LocalBinder)
            stopwatchService = binder.getService()
            stopwatchModel.state = stopwatchService.state
            stopwatchModel.currentPosition = stopwatchService.currentPosition

            stopwatchService.onStateChange = {
                stopwatchModel.state = it
            }
            stopwatchService.onPositionChange = {
                stopwatchModel.currentPosition = it
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stopwatchService.onStateChange = {}
            stopwatchService.onPositionChange = {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialTab = when (intent?.action) {
            SHOW_STOPWATCH_ACTION -> NavRoutes.Stopwatch
            AlarmClock.ACTION_SET_ALARM, AlarmClock.ACTION_SHOW_ALARMS -> NavRoutes.Alarm
            AlarmClock.ACTION_SET_TIMER, AlarmClock.ACTION_SHOW_TIMERS -> NavRoutes.Timer
            else -> bottomNavItems.first {
                Preferences.instance.getString(
                    Preferences.startTabKey,
                    NavRoutes.Alarm.route
                ) == it.route
            }
        }
        setContent {
            val settingsModel: SettingsModel = viewModel()

            val darkTheme = when (settingsModel.themeMode) {
                SettingsModel.Theme.SYSTEM -> isSystemInDarkTheme()
                SettingsModel.Theme.DARK, SettingsModel.Theme.AMOLED -> true
                else -> false
            }
            ClockYouTheme(
                darkTheme = darkTheme,
                customColorScheme = ThemeUtil.getSchemeFromSeed(
                    settingsModel.customColor,
                    darkTheme
                ),
                dynamicColor = settingsModel.colorTheme == SettingsModel.ColorTheme.SYSTEM,
                amoledDark = settingsModel.themeMode == SettingsModel.Theme.AMOLED
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    getInitialAlarm()?.let {
                        AlarmReceiverDialog(it)
                    }
                    getInitialTimer()?.let {
                        TimerReceiverDialog(it)
                    }
                    NavContainer(settingsModel, initialTab)
                }
            }

            LaunchedEffect(Unit) {
                requestNotificationPermissions()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, StopwatchService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }


    private fun getInitialAlarm(): Alarm? {
        if (intent?.action != AlarmClock.ACTION_SET_ALARM) return null

        val hours = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 0)
        val minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0)
        val days = intent.getIntArrayExtra(AlarmClock.EXTRA_DAYS)?.map { it - 1 }
        val ringingTone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE)
            .takeIf { it != AlarmClock.VALUE_RINGTONE_SILENT }

        return Alarm(
            time = ((hours * 60 + minutes) * 60000).toLong(),
            label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE),
            enabled = false,
            days = days ?: listOf(0, 1, 2, 3, 4, 5, 6),
            repeat = days != null,
            soundUri = ringingTone,
            vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE, false),
            soundEnabled = ringingTone != null
        )
    }

    private fun getInitialTimer(): Int? {
        if (intent?.action != AlarmClock.ACTION_SET_TIMER) return null

        return intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0).takeIf { it > 0 }
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }

    companion object {
        const val SHOW_STOPWATCH_ACTION = "com.bnyro.clock.SHOW_STOPWATCH_ACTION"
    }
}
