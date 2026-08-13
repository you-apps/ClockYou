package com.bnyro.clock.util.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import androidx.core.app.NotificationCompat
import com.bnyro.clock.App
import com.bnyro.clock.R
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "upcoming_alarm_channel" //insane crazy name
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getLongExtra(AlarmHelper.EXTRA_ID, -1)?.takeIf { it != -1L } ?: return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Upcoming Alarms", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val dismissIntent = Intent(context, DismissUpcomingReceiver::class.java).apply {
            putExtra(AlarmHelper.EXTRA_ID, id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt() + AlarmHelper.PRE_ALARM_ID_OFFSET,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            id.toInt() + AlarmHelper.PRE_ALARM_ID_OFFSET,
            Intent(context, MainActivity::class.java).apply {
                action = AlarmClock.ACTION_SHOW_ALARMS
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val alarmRepository = (context.applicationContext as App).container.alarmRepository
                val alarm = alarmRepository.getAlarmById(id)

                if (alarm != null) {
                    val targetAlarmTimeMs = AlarmHelper.getAlarmTime(alarm)

                    val formattedTime = TimeHelper.formatTime(
                        context,
                        java.time.Instant.ofEpochMilli(targetAlarmTimeMs)
                            .atZone(java.time.ZoneId.systemDefault())
                    )

                    val contentText = context.getString(R.string.upcoming_alarm_content, formattedTime)
                    withContext(Dispatchers.Main) {
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_alarm)
                            .setContentTitle(context.getString(R.string.upcoming_alarm))
                            .setContentText(contentText)
                            .setContentIntent(contentPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .addAction(R.drawable.ic_alarm, context.getString(R.string.dismiss), dismissPendingIntent)

                            .setOngoing(true)
                            .build()

                        notificationManager.notify(
                            id.toInt() + AlarmHelper.PRE_ALARM_ID_OFFSET,
                            notification
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}