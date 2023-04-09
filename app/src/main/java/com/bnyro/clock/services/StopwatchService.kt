package com.bnyro.clock.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper
import java.util.*

class StopwatchService : Service() {
    private val notificationId = 1

    private val binder = LocalBinder()
    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    private var currentPosition = 0
    private var state: WatchState = WatchState.IDLE
    private val updateDelay = 10

    var changeListener: (state: WatchState, time: Int) -> Unit = { _, _ -> }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, getNotification())
        state = WatchState.RUNNING

        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    handler.post(this@StopwatchService::updateState)
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

    private fun updateState() {
        if (state != WatchState.RUNNING) return

        currentPosition += updateDelay
        changeListener.invoke(state, currentPosition)
        if (currentPosition % 1000 == 0) updateNotification()
    }

    private fun updateNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId, getNotification())
        }
    }

    private fun getNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.STOPWATCH_CHANNEL
    )
        .setContentTitle(getText(R.string.stopwatch))
        .setContentText(DateUtils.formatElapsedTime((currentPosition / 1000).toLong()))
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    override fun onDestroy() {
        timer.cancel()
        changeListener = { _, _ -> }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()

        super.onDestroy()
    }

    override fun onBind(intent: Intent) = binder

    inner class LocalBinder : Binder() {
        fun getService() = this@StopwatchService
    }
}
