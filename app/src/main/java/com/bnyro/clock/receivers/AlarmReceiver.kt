package com.bnyro.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import android.widget.Toast
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import com.bnyro.clock.util.VibrationHelper
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext

        Log.e("receiver", "received")
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        val alarm = runBlocking {
            DatabaseHolder.instance.alarmsDao().findById(id)
        }

        val currentDay = TimeHelper.getCurrentWeekDay()
        if (currentDay - 1 !in alarm.days) return

        if (alarm.vibrate) {
            VibrationHelper.vibrate(context)
        }

        Log.e("vibrated", "vibrated")

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show()
        val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION)

        val ringtone = RingtoneManager.getRingtone(appContext, alarmUri)

        Log.e("got tone", "got tone")

        ringtone.play()

        Log.e("playing", "playing")
    }
}
