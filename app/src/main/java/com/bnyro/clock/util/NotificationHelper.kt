package com.bnyro.clock.util

import android.content.Context
import android.media.AudioAttributes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.clock.R

object NotificationHelper {
    const val STOPWATCH_CHANNEL = "stopwatch"
    const val TIMER_CHANNEL = "timer"
    const val TIMER_SERVICE_CHANNEL = "timer_service"
    const val TIMER_FINISHED_CHANNEL = "timer_finished"
    const val ALARM_CHANNEL = "alarm"

    val vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

    val audioAttributes: AudioAttributes? = AudioAttributes.Builder()
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
                .build(),
            NotificationChannelCompat.Builder(
                ALARM_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_MAX
            )
                .setName(context.getString(R.string.alarm))
                .build()
        )

        nManager.createNotificationChannelsCompat(channels)
    }
}
