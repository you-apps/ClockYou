package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class DeleteNotificationChannelReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val channelId = intent?.getStringExtra(NOTIFICATION_CHANNEL_ID_EXTRA) ?: return
        val nManager = NotificationManagerCompat.from(context ?: return)

        nManager.deleteNotificationChannel(channelId)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID_EXTRA = "notification_channel_id"
    }
}