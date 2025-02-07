package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.bnyro.clock.App
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import com.bnyro.clock.util.services.AlarmService
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        Log.e("receiver", "received")
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        val alarmRepository = (context.applicationContext as App).container.alarmRepository
        val alarm = runBlocking {
            alarmRepository.getAlarmById(id)
        } ?: return

        val currentDay = TimeHelper.getCurrentWeekDay()

        if (currentDay - 1 in alarm.days || !alarm.repeat) {
            val playAlarm = Intent(context, AlarmService::class.java)
            playAlarm.putExtra(AlarmHelper.EXTRA_ID, id)
            ContextCompat.startForegroundService(context, playAlarm)
        }

        // re-enqueue the alarm for the next day
        if (alarm.repeat) {
            AlarmHelper.enqueue(context, alarm)
        } else {
            alarm.enabled = false
            runBlocking {
                alarmRepository.updateAlarm(alarm)
            }
        }
    }
}
