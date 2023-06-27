package com.bnyro.clock.services

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper
import com.bnyro.clock.util.RingtoneHelper

class TimerService : ScheduleService() {
    override val notificationId = 2
    private val finishedNotificationId = 3

    override fun getNotification(scheduledObject: ScheduledObject) = NotificationCompat.Builder(
        this,
        NotificationHelper.TIMER_CHANNEL
    )
        .setContentTitle(getText(R.string.timer))
        .setUsesChronometer(scheduledObject.state.value == WatchState.RUNNING)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(true)
            } else {
                setContentText(
                    DateUtils.formatElapsedTime(
                        (scheduledObject.currentPosition.value / 1000).toLong()
                    )
                )
            }
        }
        .setWhen(System.currentTimeMillis() + scheduledObject.currentPosition.value)
        .addAction(stopAction(scheduledObject))
        .addAction(pauseResumeAction(scheduledObject))
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    override fun updateState() {
        scheduledObjects.forEach {
            if (it.state.value == WatchState.RUNNING) {
                it.currentPosition.value -= updateDelay
                invokeChangeListener()

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    updateNotification(it)
                }
            }

            if (it.currentPosition.value <= 0) {
                it.state.value = WatchState.IDLE
                invokeChangeListener()
                showFinishedNotification(it)
                stop(it)
            }
        }
    }

    private fun showFinishedNotification(scheduledObject: ScheduledObject) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(
                this,
                NotificationHelper.TIMER_FINISHED_CHANNEL
            )
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(scheduledObject.ringtone ?: RingtoneHelper.getDefault(this))
                .setContentTitle(getString(R.string.timer_finished))
                .setContentText(scheduledObject.label.value)
                .build()

            NotificationManagerCompat.from(this)
                .notify(finishedNotificationId, notification)
        }
    }

    fun updateLabel(id: Int, newLabel: String) {
        scheduledObjects.firstOrNull { it.id == id }?.let {
            it.label.value = newLabel
            invokeChangeListener()
        }
    }

    fun updateRingtone(id: Int, newRingtoneUri: Uri?) {
        scheduledObjects.firstOrNull { it.id == id }?.let {
            it.ringtone = newRingtoneUri
            invokeChangeListener()
        }
    }

    override fun getStartNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.TIMER_SERVICE_CHANNEL
    )
        .setContentTitle(getString(R.string.timer_service))
        .setSmallIcon(R.drawable.ic_notification)
        .build()
}
