package com.bnyro.clock.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.Permission
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.receivers.AlarmReceiver
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

object AlarmHelper {
    const val EXTRA_ID = "alarm_id"

    @SuppressLint("ScheduleExactAlarm")
    fun enqueue(context: Context, alarm: Alarm) {
        if (!Permission.AlarmPermission.hasPermission(context)) return
        cancel(context, alarm)
        if (!alarm.enabled) {
            Log.d("AlarmHelper", "Alarm Is disabled")
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmInfo = AlarmManager.AlarmClockInfo(
            getAlarmTime(alarm),
            getOpenAppIntent(context, alarm)
        )
        Log.d("AlarmHelper", "Scheduling alarm time: ${Date(getAlarmTime(alarm))}")
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
    fun getAlarmTime(alarm: Alarm): Long {
        val calendar = GregorianCalendar()
        calendar.time = TimeHelper.currentDateTime

        // reset the calendar time to the start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // add the milliseconds from the new alarm
        calendar.add(Calendar.MILLISECOND, alarm.time.toInt())

        calendar.add(Calendar.DATE, getPostponeDays(alarm, calendar))

        fixDaylightTime(calendar)

        return calendar.timeInMillis
    }

    fun fixDaylightTime(calendar: GregorianCalendar) {
        val now = TimeHelper.currentDateTime

        if (calendar.timeZone.useDaylightTime()) {
            if (calendar.timeZone.inDaylightTime(now) && !calendar.timeZone.inDaylightTime(calendar.time)) {
                calendar.timeInMillis += calendar.timeZone.dstSavings
            } else if (!calendar.timeZone.inDaylightTime(now) && calendar.timeZone.inDaylightTime(calendar.time)) {
                calendar.timeInMillis -= calendar.timeZone.dstSavings
            }
        }
    }

    private fun getPostponeDays(alarm: Alarm, calendar: GregorianCalendar): Int {
        if (alarm.days.isEmpty() && alarm.repeat) return 0

        val hasEventPassed = calendar.time.time < TimeHelper.currentDateTime.time

        if (alarm.repeat) {
            val today = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val eventDay = when {
                alarm.days.last() >= today -> {
                    // Get the next alarm
                    val day = alarm.days.first { it >= today }
                    when {
                        // If the alarm is not set up for today or is setup for today and it hasn't ringed yet, do nothing
                        day > today || (day == today && !hasEventPassed) -> day
                        // If there was an alarm today but it already ringed and there is more in the weekend, skip to the next one.
                        day == today && alarm.days.last() > today -> alarm.days.first { it > today }
                        else -> alarm.days.first()
                    }
                }

                else -> alarm.days.first()
            }
            var dayDiff = eventDay - today
            // If an alarm is set on repeat but only set up for one day, check if has already played and reset the days accordingly
            if (dayDiff < 0 || (hasEventPassed && dayDiff == 0)) dayDiff += 7
            return dayDiff
        }

        // the alarm is a one time alarm and hence the day only needs to be incremented when it's not today
        return if (hasEventPassed) 1 else 0
    }

    fun snooze(context: Context, oldAlarm: Alarm, snoozeMinutes: Int = oldAlarm.snoozeMinutes) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, snoozeMinutes)

        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)

        val newTime = (hours * 60 + minutes) * 60 * 1000L
        enqueue(context, oldAlarm.copy(time = newTime, enabled = true))
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
