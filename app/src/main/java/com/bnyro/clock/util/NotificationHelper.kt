package com.bnyro.clock.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.clock.R

object NotificationHelper {
    const val STOPWATCH_CHANNEL = "stopwatch"
    const val TIMER_CHANNEL = "timer"
    const val TIMER_FINISHED_CHANNEL = "timer_finished"
    const val ALARM_CHANNEL = "alarm"

    fun createNotificationChannels(context: Context) {
        val nManager = NotificationManagerCompat.from(context)

        val channels = listOf(
            NotificationChannelCompat.Builder(
                STOPWATCH_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
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
                TIMER_FINISHED_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(context.getString(R.string.timer_finished))
                .build()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            NotificationChannel(
                ALARM_CHANNEL,
                context.getString(R.string.alarm),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                setSound(RingtoneHelper.getUri(context), audioAttributes)
            }.let {
                nManager.createNotificationChannel(it)
            }
        }

        nManager.createNotificationChannelsCompat(channels)
    }
}
