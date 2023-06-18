package com.bnyro.clock.receivers

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.bnyro.clock.R
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.*
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("receiver", "received")
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        val alarm = runBlocking {
            DatabaseHolder.instance.alarmsDao().findById(id)
        }

        val currentDay = TimeHelper.getCurrentWeekDay()

        if (currentDay - 1 in alarm.days) {
            if (alarm.vibrate) VibrationHelper.vibrate(context)
            Toast.makeText(context, context.getString(R.string.alarm), Toast.LENGTH_LONG).show()
            showNotification(context, alarm)
        }

        // re-enqueue the alarm for the next day
        AlarmHelper.enqueue(context, alarm)
    }

    private fun showNotification(context: Context, alarm: Alarm) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationHelper.createAlarmNotificationChannel(context, alarm)

        val builder = NotificationCompat.Builder(context, NotificationHelper.ALARM_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(alarm.label ?: context.getString(R.string.alarm))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setSound(alarm.soundUri?.toUri() ?: RingtoneHelper.getDefault(context))
        val notification = builder.build().apply {
            flags = NotificationCompat.FLAG_INSISTENT
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(alarm.id.toInt(), notification)
        }
    }
}
