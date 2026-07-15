package com.bnyro.clock.util.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import com.bnyro.clock.App
import com.bnyro.clock.R
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val PRE_ALARM_OFFSET = 4000
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
            id.toInt() + PRE_ALARM_OFFSET,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val alarmRepository = (context.applicationContext as App).container.alarmRepository
                val alarm = alarmRepository.getAlarmById(id)

                if (alarm != null) {
                    val targetAlarmTimeMs = AlarmHelper.getAlarmTime(alarm)

                    val formattedTime = DateUtils.formatDateTime(
                        context,
                        targetAlarmTimeMs,
                        DateUtils.FORMAT_SHOW_TIME
                    )

                    val contentText = context.getString(R.string.upcoming_alarm_content, formattedTime)
                    withContext(Dispatchers.Main) {
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_alarm)
                            .setContentTitle(context.getString(R.string.upcoming_alarm))
                            .setContentText(contentText)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .addAction(R.drawable.ic_alarm, context.getString(R.string.skip), dismissPendingIntent)
                            .setAutoCancel(true)
                            .build()

                        notificationManager.notify(id.toInt() + PRE_ALARM_OFFSET, notification)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}