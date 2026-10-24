package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.bnyro.clock.App
import com.bnyro.clock.util.AlarmHelper
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

        // the alarm rang its last occurrence, so it may not be re-enqueued for another one
        if (AlarmHelper.hasRecurrenceEnded(alarm)) {
            alarm.enabled = false
            runBlocking {
                alarmRepository.updateAlarm(alarm)
            }
        }

        val playAlarm = Intent(context, AlarmService::class.java)
        playAlarm.putExtra(AlarmHelper.EXTRA_ID, id)
        ContextCompat.startForegroundService(context, playAlarm)

        AlarmHelper.enqueue(context, alarm)
    }
}
