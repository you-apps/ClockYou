package com.bnyro.clock.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.bnyro.clock.R
import com.bnyro.clock.obj.Alarm

object NotificationHelper {
    const val STOPWATCH_CHANNEL = "stopwatch"
    const val TIMER_CHANNEL = "timer"
    const val TIMER_SERVICE_CHANNEL = "timer_service"
    const val TIMER_FINISHED_CHANNEL = "timer_finished"
    const val ALARM_CHANNEL = "alarm"

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    fun createNotificationChannels(context: Context) {
        val nManager = NotificationManagerCompat.from(context)

        val channels = listOf(
            NotificationChannelCompat.Builder(
                STOPWATCH_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(context.getString(R.string.stopwatch))
                .build(),
            NotificationChannelCompat.Builder(
                TIMER_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(context.getString(R.string.timer))
                .build(),
            NotificationChannelCompat.Builder(
                TIMER_SERVICE_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(context.getString(R.string.timer_service))
                .build(),
            NotificationChannelCompat.Builder(
                TIMER_FINISHED_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(context.getString(R.string.timer_finished))
                .setSound(RingtoneHelper.getDefault(context), audioAttributes)
                .build()
        )

        nManager.createNotificationChannelsCompat(channels)
    }

    fun createAlarmNotificationChannel(context: Context, alarm: Alarm) {
        val nManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                ALARM_CHANNEL,
                context.getString(R.string.alarm),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                val soundUri = alarm.soundUri?.toUri() ?: RingtoneHelper.getDefault(context)
                setBypassDnd(true)
                setSound(soundUri, audioAttributes)
            }.let {
                nManager.createNotificationChannel(it)
            }
        }
    }
}
