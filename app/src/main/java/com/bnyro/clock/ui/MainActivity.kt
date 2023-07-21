package com.bnyro.clock.ui

import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.dialog.AlarmReceiverDialog
import com.bnyro.clock.dialog.TimerReceiverDialog
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.nav.NavContainer
import com.bnyro.clock.ui.nav.NavRoutes
import com.bnyro.clock.ui.theme.ClockYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsModel: SettingsModel = viewModel()

            Log.e("action", intent?.action.toString())
            val initialTab = when (intent?.action) {
                AlarmClock.ACTION_SET_ALARM, AlarmClock.ACTION_SHOW_ALARMS -> NavRoutes.Alarm
                AlarmClock.ACTION_SET_TIMER, AlarmClock.ACTION_SHOW_TIMERS -> NavRoutes.Timer
                else -> NavRoutes.Clock
            }

            ClockYouTheme(
                darkTheme = when (settingsModel.themeMode) {
                    "system" -> isSystemInDarkTheme()
                    else -> settingsModel.themeMode == "dark"
                }
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
        }
    }

    private fun getInitialAlarm(): Alarm? {
        if (intent?.action != AlarmClock.ACTION_SET_ALARM) return null

        val hours = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 0)
        val minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0)
        val days = intent.getIntArrayExtra(AlarmClock.EXTRA_DAYS)?.map { it - 1 }
            ?: listOf(0, 1, 2, 3, 4, 5, 6)

        return Alarm(
            time = ((hours * 60 + minutes) * 60000).toLong(),
            label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE),
            enabled = false,
            days = days,
            soundUri = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE),
            vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE, false)
        )
    }

    private fun getInitialTimer(): Int? {
        if (intent?.action != AlarmClock.ACTION_SET_TIMER) return null

        return intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0).takeIf { it > 0 }
    }
}
