package com.bnyro.clock.services

import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper

class StopwatchService : ScheduleService() {
    override val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        scheduledObjects.add(
            ScheduledObject(
                state = mutableStateOf(WatchState.RUNNING),
                id = notificationId
            )
        )
    }

    override fun updateState() {
        scheduledObjects.forEach {
            if (it.state.value == WatchState.RUNNING) {
                it.currentPosition.value += updateDelay
                invokeChangeListener()
            }
        }
    }

    override fun getNotification(scheduledObject: ScheduledObject) = NotificationCompat.Builder(
        this,
        NotificationHelper.STOPWATCH_CHANNEL
    )
        .setContentTitle(getText(R.string.stopwatch))
        .setUsesChronometer(scheduledObject.state.value == WatchState.RUNNING)
        .setWhen(System.currentTimeMillis() - scheduledObject.currentPosition.value)
        .addAction(stopAction(scheduledObject))
        .addAction(pauseResumeAction(scheduledObject))
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    override fun getStartNotification() = getNotification(ScheduledObject())
}
