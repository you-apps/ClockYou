package com.bnyro.clock.util

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.clock.R

object NotificationHelper {
    const val STOPWATCH_CHANNEL = "stopwatch"
    const val TIMER_CHANNEL = "timer"
    const val TIMER_FINISHED_CHANNEL = "timer_finished"

    fun createNotificationChannels(context: Context) {
        val nManager = NotificationManagerCompat.from(context)

        val channels = listOf(
            NotificationChannelCompat.Builder(STOPWATCH_CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.stopwatch))
                .build(),
            NotificationChannelCompat.Builder(TIMER_CHANNEL, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.timer))
                .build(),
            NotificationChannelCompat.Builder(TIMER_FINISHED_CHANNEL, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.timer_finished))
                .build()
        )
        nManager.createNotificationChannelsCompat(channels)
    }
}
