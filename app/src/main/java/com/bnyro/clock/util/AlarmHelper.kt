package com.bnyro.clock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.receivers.AlarmReceiver
import com.bnyro.clock.ui.MainActivity
import java.util.Calendar
import java.util.GregorianCalendar

object AlarmHelper {
    const val EXTRA_ID = "alarm_id"
    val availableDays = listOf("S", "M", "T", "W", "T", "F", "S")

    fun enqueue(context: Context, alarm: Alarm) {
        cancel(context, alarm)
        if (!alarm.enabled) {
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmInfo = AlarmManager.AlarmClockInfo(
            getAlarmTime(alarm),
            getOpenAppIntent(context, alarm)
        )
        alarmManager.setAlarmClock(alarmInfo, getPendingIntent(context, alarm))
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

    private fun getOpenAppIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context.applicationContext, MainActivity::class.java)
            .putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getActivity(
            context.applicationContext,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Calculate the epoch time for scheduling an alarm
     */
    private fun getAlarmTime(alarm: Alarm): Long {
        val calendar = GregorianCalendar()
        calendar.time = TimeHelper.currentTime

        // reset the calendar time to the start of the day
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // add the milliseconds from the new alarm
        calendar.add(Calendar.MILLISECOND, alarm.time.toInt())

        // if the event has already passed for the day, schedule for the next day
        if (calendar.time.time < TimeHelper.currentTime.time) {
            calendar.add(Calendar.HOUR, 24)
        }
        return calendar.timeInMillis
    }
}
