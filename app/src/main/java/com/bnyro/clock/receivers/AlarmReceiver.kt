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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
   private val dbHolder by lazy { DatabaseHolder.instance }

   override fun onReceive(context: Context, intent: Intent) {
       Log.e("receiver", "received")
       val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
       GlobalScope.launch {
           val alarm = dbHolder.alarmsDao().findById(id)

           val currentDay = TimeHelper.getCurrentWeekDay()

           if (currentDay - 1 in alarm.days || !alarm.repeat) {
               val playAlarm = Intent(context, AlarmService::class.java)
               playAlarm.putExtra(AlarmHelper.EXTRA_ID, id)
               if (!isMyServiceRunning(AlarmService::class.java, context)) {
                  ContextCompat.startForegroundService(context, playAlarm)
               }
           }

           // re-enqueue the alarm for the next day
           if (alarm.repeat) {
               AlarmHelper.enqueue(context, alarm)
           } else {
               alarm.enabled = false
               dbHolder.alarmsDao().update(alarm)
           }
       }
   }

   private fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
       val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
       for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
           if (serviceClass.name == service.service.className) {
               return true
           }
       }
       return false
   }
}
