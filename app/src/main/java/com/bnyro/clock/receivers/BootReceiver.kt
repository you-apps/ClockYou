package com.bnyro.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {
   override fun onReceive(context: Context, intent: Intent) {
       CoroutineScope(Dispatchers.Main).launch {
           val alarms = withContext(Dispatchers.IO) {
               DatabaseHolder.instance.alarmsDao().getAll()
           }
           alarms.forEach {
               AlarmHelper.enqueue(context, it)
           }
       }
   }
}
