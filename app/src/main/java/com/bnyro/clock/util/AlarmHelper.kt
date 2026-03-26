package com.bnyro.clock.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.Permission
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.receivers.AlarmReceiver
import com.bnyro.clock.util.receivers.PreAlarmReceiver
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
//schweiny ass file
object AlarmHelper {
    const val EXTRA_ID = "alarm_id"
    private const val DAYS_PER_WEEK = 7
    const val PRE_ALARM_ID_OFFSET = 4000
    private const val PRE_ALARM_DELAY = 10800000L  //CHANGE this to change delay maybe in settings later BUDDY

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ScheduleExactAlarm")
    fun enqueue(context: Context, alarm: Alarm, skipToday: Boolean = false) {
        if (!Permission.AlarmPermission.hasPermission(context)) return
        cancel(context, alarm)
        if (!alarm.enabled) {
            Log.d("AlarmHelper", "Alarm Is disabled")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = getAlarmTime(alarm, skipToday)

        val alarmInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            getOpenAppIntent(context, alarm)
        )

        Log.d("AlarmHelper", "Scheduling alarm time: ${Date(getAlarmTime(alarm))}")
        alarmManager.setAlarmClock(alarmInfo, getPendingIntent(context, alarm))

        val preAlarmTime = triggerTime - PRE_ALARM_DELAY
        if (preAlarmTime > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                preAlarmTime,
                getPreAlarmPendingIntent(context, alarm)
            )
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getPendingIntent(context, alarm))
        alarmManager.cancel(getPreAlarmPendingIntent(context, alarm))
    }

    fun cancel(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val mainIntent = Intent(context.applicationContext,
            AlarmReceiver::class.java).putExtra(EXTRA_ID, id)




        val mainPi = PendingIntent.getBroadcast(context.applicationContext,
            id.toInt(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val preIntent = Intent(context.applicationContext,
            PreAlarmReceiver::class.java).putExtra(EXTRA_ID, id)





        val prePi = PendingIntent.getBroadcast(context.applicationContext,
            id.toInt() + PRE_ALARM_ID_OFFSET, preIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(mainPi)
        alarmManager.cancel(prePi)
    }

    private fun getPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context.applicationContext,
            AlarmReceiver::class.java).putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getBroadcast(context.applicationContext, alarm.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }


    private fun getPreAlarmPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context.applicationContext,
            PreAlarmReceiver::class.java).putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getBroadcast(context.applicationContext,
            alarm.id.toInt() + PRE_ALARM_ID_OFFSET, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }


    private fun getOpenAppIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context.applicationContext,
            MainActivity::class.java).putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getActivity(context.applicationContext,
            alarm.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    /**
     * Calculate the epoch time for scheduling an alarm
     */

    fun getAlarmTime(alarm: Alarm, skipToday: Boolean = false): Long {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.DATE, getPostponeDays(alarm, skipToday))

        val (hours, minutes, _, _) = TimeHelper.millisToTime(alarm.time)
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)

        return calendar.timeInMillis
    }

    private fun getPostponeDays(alarm: Alarm, skipToday: Boolean = false): Int {
        if (alarm.days.isEmpty() && alarm.repeat) return 0

        val currentTime = GregorianCalendar().apply { time = TimeHelper.currentDateTime }
        val currentDay = currentTime.get(Calendar.DAY_OF_WEEK) - 1
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val alarmTime = TimeHelper.millisToTime(alarm.time)

        val hasEventPassed = skipToday || currentHour > alarmTime.hours ||
                (alarmTime.hours == currentHour && currentMinute >= alarmTime.minutes)

        if ((currentDay in alarm.days || !alarm.repeat) && !hasEventPassed) return 0
        if (!alarm.repeat) return 1

        val nextDay = alarm.days.firstOrNull { it > currentDay } ?: (alarm.days.first() + DAYS_PER_WEEK)
        return nextDay - currentDay
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun snooze(context: Context, oldAlarm: Alarm, snoozeMinutes: Int = oldAlarm.snoozeMinutes) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, snoozeMinutes)
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val newTime = (hours * 60 + minutes) * 60 * 1000L
        enqueue(context, oldAlarm.copy(time = newTime, enabled = true, repeat = false))
    }
    /**
     * @return the days of the week mapped to an index 0-Sunday, 1-Monday, ..., 6-Saturday.
     * The list order will match the user preferred days of the week order.
     */

    fun getDaysOfWeekByLocale(context: Context): List<Pair<String, Int>> {
        val availableDays = context.resources.getStringArray(R.array.available_days).toList()
        val firstDayIndex = GregorianCalendar().firstDayOfWeek - 1
        val daysWithIndex = availableDays.mapIndexed { index, s -> s to index }
        return daysWithIndex.subList(firstDayIndex, 7) + daysWithIndex.subList(0, firstDayIndex)
    }
}