package com.bnyro.clock.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.RepeatAnchor
import com.bnyro.clock.domain.model.Permission
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.receivers.AlarmReceiver
import com.bnyro.clock.util.receivers.PreAlarmReceiver
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
//schweiny ass file
object AlarmHelper {
    const val EXTRA_ID = "alarm_id"
    private const val DAYS_PER_WEEK = 7
    private const val MONTHS_PER_YEAR = 12
    const val PRE_ALARM_ID_OFFSET = 4000
    const val PRE_ALARM_DELAY = 10800000L  //CHANGE this to change delay maybe in settings later BUDDY

    fun showAlarmScheduledToast(context: Context, alarm: Alarm) {
        val alarmTime = getAlarmTime(alarm) ?: return
        val millisRemaining = alarmTime - System.currentTimeMillis()
        Toast.makeText(
            context,
            if (millisRemaining <= 0) {
                context.getString(R.string.alarm_starting_now)
            } else {
                context.getString(
                    R.string.alarm_will_play,
                    TimeHelper.durationToFormatted(context, millisRemaining.milliseconds)
                )
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun enqueue(context: Context, alarm: Alarm, skipToday: Boolean = false) {
        if (!Permission.AlarmPermission.hasPermission(context)) return
        cancel(context, alarm)
        if (!alarm.enabled) {
            Log.d("AlarmHelper", "Alarm Is disabled")
            return
        }
        if (hasRecurrenceEnded(alarm)) {
            Log.d("AlarmHelper", "Alarm has no occurrence left")
            return
        }

        schedule(context, alarm, getAlarmTime(alarm, skipToday) ?: return)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ScheduleExactAlarm")
    private fun schedule(context: Context, alarm: Alarm, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            getOpenAppIntent(context, alarm)
        )

        Log.d("AlarmHelper", "Scheduling alarm time: ${Date(triggerTime)}")
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

    fun getAlarmTime(alarm: Alarm, skipToday: Boolean = false): Long? {
        val (hours, minutes, _, _) = TimeHelper.millisToTime(alarm.time)
        return getNextOccurrence(alarm, skipToday)
            ?.atTime(hours, minutes)
            ?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    }

    /**
     * @return the day the alarm rings next, skipping the occurrence the user dismissed upfront,
     * or null when its repetition never lets it ring.
     */
    fun getNextOccurrence(alarm: Alarm, skipToday: Boolean = false): LocalDate? {
        val (hours, minutes, _, _) = TimeHelper.millisToTime(alarm.time)
        val now = LocalDateTime.now()
        val hasEventPassed = now.toLocalTime()
            .truncatedTo(ChronoUnit.MINUTES) >= LocalTime.of(hours, minutes)
        val earliestDate = if (skipToday || hasEventPassed) {
            now.toLocalDate().plusDays(1)
        } else {
            now.toLocalDate()
        }

        val occurrence =
            occurrenceOnOrAfter(alarm, maxOf(earliestDate, LocalDate.ofEpochDay(alarm.startDate)))
                ?: return null
        val dismissedOccurrence = alarm.dismissedAt?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        if (skipToday || occurrence != dismissedOccurrence) return occurrence

        return occurrenceOnOrAfter(alarm, occurrence.plusDays(1))
    }

    /**
     * @return the day the repetition that rings next begins on. Arming an alarm anchors it here
     * rather than at the occurrence itself, which would shift every later repetition.
     */
    fun getNextRepetitionStart(alarm: Alarm): LocalDate? =
        getNextOccurrence(alarm)?.let { runStartsOnOrBefore(alarm, it).first() }

    /**
     * @return up to [limit] of the first days the alarm rings on, from its start date onwards,
     * stopping early where its repetition ends or never lets it ring at all.
     */
    fun getOccurrences(alarm: Alarm, limit: Int): List<LocalDate> {
        val endDate = alarm.endDate?.let { LocalDate.ofEpochDay(it) }
        val occurrences = mutableListOf<LocalDate>()
        var from = LocalDate.ofEpochDay(alarm.startDate)

        repeat(minOf(limit, alarm.endOccurrences ?: limit)) {
            val occurrence = occurrenceOnOrAfter(alarm, from) ?: return occurrences
            if (endDate != null && occurrence > endDate) return occurrences
            occurrences += occurrence
            from = occurrence.plusDays(1)
        }
        return occurrences
    }

    /**
     * @return whether the alarm has rung all the occurrences its repetition allows for. An alarm
     * whose repetition never lets it ring has not ended, it simply stays silent.
     */
    fun hasRecurrenceEnded(alarm: Alarm): Boolean {
        val nextOccurrence = getNextOccurrence(alarm) ?: return false
        alarm.endDate?.let { if (nextOccurrence > LocalDate.ofEpochDay(it)) return true }
        alarm.endOccurrences?.let { count ->
            val last = lastOccurrence(alarm, count) ?: return false
            return nextOccurrence > last
        }
        return false
    }

    /**
     * @return the first day on or after [from] that the alarm rings on, or null when its
     * repetition never lets it ring. Every repetition starts a run that lasts for the duration the
     * alarm repeats for, and the alarm rings on each day of that run a weekly repetition also
     * selects.
     */
    private fun occurrenceOnOrAfter(alarm: Alarm, from: LocalDate): LocalDate? {
        val runStarts = runStartsOnOrBefore(alarm, from)
        if (!ringsWithinRun(alarm, runStarts.first())) return null

        return runStarts.firstNotNullOf { runStart ->
            val runEnd = runEnd(alarm, runStart)
            generateSequence(maxOf(from, runStart)) { it.plusDays(1) }
                .takeWhile { it < runEnd }
                .firstOrNull { ringsOn(alarm, it) }
        }
    }

    /**
     * @return the day each repetition of the alarm starts its run on, beginning with the last one
     * that starts on or before [from].
     */
    private fun runStartsOnOrBefore(alarm: Alarm, from: LocalDate): Sequence<LocalDate> {
        val startDate = LocalDate.ofEpochDay(alarm.startDate)
        val interval = alarm.repeatInterval.toLong()

        return when (alarm.repeatUnit) {
            RepeatUnit.DAY -> {
                val elapsed = ChronoUnit.DAYS.between(startDate, from) / interval
                generateSequence(startDate.plusDays(elapsed * interval)) { it.plusDays(interval) }
            }

            RepeatUnit.WEEK -> {
                val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                val startWeek = startDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                val elapsed = ChronoUnit.WEEKS.between(
                    startWeek,
                    from.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                ) / interval
                generateSequence(startWeek.plusWeeks(elapsed * interval)) { it.plusWeeks(interval) }
            }

            RepeatUnit.MONTH, RepeatUnit.YEAR -> {
                val startMonth = YearMonth.from(startDate)
                val weekOfMonth = (startDate.dayOfMonth - 1) / DAYS_PER_WEEK + 1
                val months =
                    if (alarm.repeatUnit == RepeatUnit.YEAR) interval * MONTHS_PER_YEAR else interval
                val elapsed = ChronoUnit.MONTHS.between(startMonth, YearMonth.from(from)) / months
                generateSequence(startMonth.plusMonths(elapsed * months)) {
                    it.plusMonths(months)
                }.mapNotNull { month ->
                    val day = when (alarm.repeatAnchor) {
                        RepeatAnchor.DAY_OF_MONTH ->
                            month.atDay(minOf(startDate.dayOfMonth, month.lengthOfMonth()))

                        RepeatAnchor.DAY_OF_WEEK -> month.atDay(1).with(
                            TemporalAdjusters.dayOfWeekInMonth(weekOfMonth, startDate.dayOfWeek)
                        )
                    }
                    day.takeIf { YearMonth.from(it) == month }
                }
            }
        }
    }

    /**
     * @return the day after the last one a run beginning at [runStart] rings on.
     */
    private fun runEnd(alarm: Alarm, runStart: LocalDate): LocalDate {
        val duration = alarm.repeatDuration?.toLong() ?: return when (alarm.repeatUnit) {
            RepeatUnit.WEEK -> runStart.plusWeeks(1)
            else -> runStart.plusDays(1)
        }

        return when (alarm.repeatDurationUnit) {
            RepeatUnit.DAY -> runStart.plusDays(duration)
            RepeatUnit.WEEK -> runStart.plusWeeks(duration)
            RepeatUnit.MONTH -> runStart.plusMonths(duration)
            RepeatUnit.YEAR -> runStart.plusYears(duration)
        }
    }

    private fun ringsOn(alarm: Alarm, date: LocalDate): Boolean =
        alarm.repeatUnit != RepeatUnit.WEEK || date.dayOfWeek.value % DAYS_PER_WEEK in alarm.days

    /**
     * Every run of a weekly repetition covers the same weekdays, so a run that selects none of
     * them never will, however far ahead the alarm is searched.
     */
    private fun ringsWithinRun(alarm: Alarm, runStart: LocalDate): Boolean =
        generateSequence(runStart) { it.plusDays(1) }
            .takeWhile { it < runEnd(alarm, runStart) }
            .any { ringsOn(alarm, it) }

    /**
     * @return the day of the [count]th occurrence, counted from the start date of the alarm, or
     * null when the alarm never rings.
     */
    private fun lastOccurrence(alarm: Alarm, count: Int): LocalDate? {
        var occurrence = occurrenceOnOrAfter(alarm, LocalDate.ofEpochDay(alarm.startDate))
        repeat(count - 1) {
            occurrence = occurrence?.let { occurrenceOnOrAfter(alarm, it.plusDays(1)) }
        }
        return occurrence
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun snooze(context: Context, oldAlarm: Alarm, snoozeMinutes: Int = oldAlarm.snoozeMinutes) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, snoozeMinutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        schedule(context, oldAlarm, calendar.timeInMillis)
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
