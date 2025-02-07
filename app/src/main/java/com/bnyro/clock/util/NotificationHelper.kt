package com.bnyro.clock.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.clock.R
import com.bnyro.clock.util.receivers.DeleteNotificationChannelReceiver

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

    /**
     * Create a temporary dynamic notification channel.
     *
     * The returned intent must be called when the notification is dismissed to delete the channel!
     */
    fun createDynamicChannel(
        context: Context,
        @StringRes nameRes: Int,
        channelId: String,
        ringtoneUri: Uri?,
        vibrationPattern: LongArray?
    ): Intent {
        val nManager = NotificationManagerCompat.from(context)

        val channel = NotificationChannelCompat.Builder(
            channelId,
            NotificationManagerCompat.IMPORTANCE_MAX
        )
            .setName(context.getString(nameRes))

        if (ringtoneUri != null) channel.setSound(ringtoneUri, audioAttributes)
        if (vibrationPattern != null) channel.setVibrationPattern(vibrationPattern)
        channel.setVibrationEnabled(vibrationPattern != null)

        nManager.createNotificationChannel(channel.build())

        return Intent(context, DeleteNotificationChannelReceiver::class.java)
            .putExtra(
                DeleteNotificationChannelReceiver.NOTIFICATION_CHANNEL_ID_EXTRA,
                channelId
            )
    }

    fun createStaticNotificationChannels(context: Context) {
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
                ALARM_CHANNEL,
                NotificationManagerCompat.IMPORTANCE_MAX
            )
                .setName(context.getString(R.string.alarm))
                .build()
        )

        nManager.createNotificationChannelsCompat(channels)
    }
}
