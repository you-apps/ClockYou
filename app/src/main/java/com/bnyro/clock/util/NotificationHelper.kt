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

   private val notificationManager = NotificationManagerCompat.from(context)

   fun createNotificationChannels(context: Context) {
       val builder = NotificationChannelCompat.Builder(
           "",
           NotificationManagerCompat.IMPORTANCE_LOW
       )

       val channels = listOf(
           STOPWATCH_CHANNEL,
           TIMER_CHANNEL,
           TIMER_SERVICE_CHANNEL,
           TIMER_FINISHED_CHANNEL,
           ALARM_CHANNEL
       ).map {
           builder.setId(it)
               .setName(context.getString(when (it) {
                  STOPWATCH_CHANNEL -> R.string.stopwatch
                  TIMER_CHANNEL -> R.string.timer
                  TIMER_SERVICE_CHANNEL -> R.string.timer_service
                  TIMER_FINISHED_CHANNEL -> R.string.timer_finished
                  ALARM_CHANNEL -> R.string.alarm
                  else -> R.string.empty
               }))
               .apply {
                  if (it == TIMER_FINISHED_CHANNEL) {
                      setSound(RingtoneHelper.getDefault(context), audioAttributes)
                  }
               }
               .build()
       }

       notificationManager.createNotificationChannelsCompat(channels)
   }
}
