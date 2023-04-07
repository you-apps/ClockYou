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
import androidx.core.os.postDelayed
import com.bnyro.clock.R
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.util.NotificationHelper

class TimerService : Service() {
    private val notificationId = 2
    private val finishedNotificationId = 3

    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val handlerToken = "timerServiceRunnable"

    private var timeLeft = 0
    private var state: WatchState = WatchState.IDLE
    private val updateDelay = 10

    var changeListener: (state: WatchState, time: Int) -> Unit = { _, _ -> }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, getNotification())
        state = WatchState.RUNNING
        handler.postDelayed(this::updateState, updateDelay.toLong())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        timeLeft = intent.getIntExtra(START_TIME_KEY, -1)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.TIMER_CHANNEL
    )
        .setContentTitle(getText(R.string.timer))
        .setContentText(DateUtils.formatElapsedTime((timeLeft / 1000).toLong()))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .build()

    fun pause() {
        state = WatchState.PAUSED
        changeListener.invoke(state, timeLeft)
    }

    fun resume() {
        state = WatchState.RUNNING
        changeListener.invoke(state, timeLeft)
    }

    fun stop() {
        state = WatchState.IDLE
        changeListener.invoke(state, timeLeft)
        onDestroy()
    }

    private fun updateState() {
        handler.postDelayed(updateDelay.toLong(), handlerToken, this::updateState)
        if (state != WatchState.RUNNING) return

        timeLeft -= updateDelay
        changeListener.invoke(state, timeLeft)
        if (timeLeft <= 0) {
            state = WatchState.IDLE
            changeListener.invoke(state, 0)
            handler.removeCallbacksAndMessages(handlerToken)
            showFinishedNotification()
            onDestroy()
        } else if (timeLeft % 1000 == 0) updateNotification()
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

    private fun showFinishedNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(
                this,
                NotificationHelper.TIMER_FINISHED_CHANNEL
            )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.timer_finished))
                .build()

            NotificationManagerCompat.from(this)
                .notify(finishedNotificationId, notification)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(handlerToken)
        changeListener = { _, _ -> }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()

        super.onDestroy()
    }

    override fun onBind(intent: Intent) = binder

    inner class LocalBinder : Binder() {
        fun getService() = this@TimerService
    }

    companion object {
        const val START_TIME_KEY = "start_time"
    }
}
