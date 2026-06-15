package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bnyro.clock.App
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeZoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        if (action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_TIME_CHANGED) {
            Log.d("TimeZoneReceiver", "time zone change")
            val appContext = context.applicationContext
            val alarmRepository = (appContext as App).container.alarmRepository
            CoroutineScope(Dispatchers.IO).launch {

                    val alarms = alarmRepository.getAlarms()
                    alarms.forEach { alarm ->
                        if (alarm.enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                AlarmHelper.enqueue(appContext, alarm)
                            }
                        }
                    }

            }
        }
    }
}