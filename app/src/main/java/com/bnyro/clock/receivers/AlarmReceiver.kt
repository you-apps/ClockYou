package com.bnyro.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.services.AlarmService
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("receiver", "received")
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        val alarm = runBlocking {
            DatabaseHolder.instance.alarmsDao().findById(id)
        }

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
                DatabaseHolder.instance.alarmsDao().update(alarm)
            }
        }
    }
}
