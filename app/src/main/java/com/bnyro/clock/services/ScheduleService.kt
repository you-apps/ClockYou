package com.bnyro.clock.services

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.clock.obj.WatchState
import java.util.*

abstract class ScheduleService : Service() {
    abstract val notificationId: Int

    private val binder = LocalBinder()
    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    var currentPosition = 0
    var state: WatchState = WatchState.IDLE
    val updateDelay = 10

    var changeListener: (state: WatchState, time: Int) -> Unit = { _, _ -> }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, getNotification())
        state = WatchState.RUNNING

        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    handler.post(this@ScheduleService::updateState)
                }
            },
            0,
            updateDelay.toLong()
        )
    }

    fun pause() {
        state = WatchState.PAUSED
        changeListener.invoke(state, currentPosition)
    }

    fun resume() {
        state = WatchState.RUNNING
        changeListener.invoke(state, currentPosition)
    }

    fun stop() {
        state = WatchState.IDLE
        changeListener.invoke(state, currentPosition)
        onDestroy()
    }

    abstract fun updateState()

    fun updateNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(notificationId, getNotification())
        }
    }

    abstract fun getNotification(): Notification

    override fun onDestroy() {
        timer.cancel()
        changeListener = { _, _ -> }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()

        super.onDestroy()
    }

    override fun onBind(intent: Intent) = binder

    inner class LocalBinder : Binder() {
        fun getService() = this@ScheduleService
    }
}
