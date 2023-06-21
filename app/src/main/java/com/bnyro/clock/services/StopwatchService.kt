package com.bnyro.clock.services

import androidx.core.app.NotificationCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper

class StopwatchService : ScheduleService() {
    override val notificationId = 1

    override fun updateState() {
        if (state == WatchState.RUNNING) {
            currentPosition += updateDelay
            invokeChangeListener()
        }
    }
    override fun getNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.STOPWATCH_CHANNEL
    )
        .setContentTitle(getText(R.string.stopwatch))
        .setUsesChronometer(state == WatchState.RUNNING)
        .setWhen(System.currentTimeMillis() - currentPosition)
        .addAction(getAction(R.string.stop, ACTION_STOP, 4))
        .addAction(pauseResumeAction())
        .setSmallIcon(R.drawable.ic_notification)
        .build()
}
