package com.bnyro.clock.services

import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper
import java.util.*

class StopwatchService : ScheduleService() {
    override val notificationId = 1

    override fun updateState() {
        if (state != WatchState.RUNNING) return

        currentPosition += updateDelay
        changeListener.invoke(state, currentPosition)
        if (currentPosition % 1000 == 0) updateNotification()
    }
    override fun getNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.STOPWATCH_CHANNEL
    )
        .setContentTitle(getText(R.string.stopwatch))
        .setContentText(DateUtils.formatElapsedTime((currentPosition / 1000).toLong()))
        .setSmallIcon(R.drawable.ic_notification)
        .build()
}
