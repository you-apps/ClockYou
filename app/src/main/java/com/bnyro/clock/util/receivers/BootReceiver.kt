package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bnyro.clock.App
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmRepository = (context.applicationContext as App).container.alarmRepository
        val alarms = runBlocking(Dispatchers.IO) {
            alarmRepository.getAlarms()
        }
        alarms.forEach {
            AlarmHelper.enqueue(context, it)
        }
    }
}
