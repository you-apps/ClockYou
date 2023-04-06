package com.bnyro.clock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.receivers.AlarmReceiver

object AlarmHelper {
    private const val RUN_DAILY = (24 * 60 * 60 * 1000).toLong()
    const val EXTRA_ID = "alarm_id"

    fun enqueue(context: Context, alarm: Alarm) {
        cancel(context, alarm)
        if (!alarm.enabled) {
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarm.time,
            RUN_DAILY,
            getPendingIntent(context, alarm)
        )
    }

    fun cancel(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getPendingIntent(context, alarm))
    }

    private fun getPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java)
            .putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
