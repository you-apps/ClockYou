package com.bnyro.clock.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.clock.R
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

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(ACTION_EXTRA_KEY)) {
                ACTION_STOP -> stop()
                ACTION_PAUSE_RESUME -> {
                    if (state == WatchState.PAUSED) resume() else pause()
                }
            }
        }
    }

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

        registerReceiver(receiver, IntentFilter(UPDATE_STATE_ACTION))
    }

    fun invokeChangeListener() {
        changeListener.invoke(state, currentPosition)
    }

    fun pause() {
        state = WatchState.PAUSED
        invokeChangeListener()
        updateNotification()
    }

    fun resume() {
        state = WatchState.RUNNING
        invokeChangeListener()
        updateNotification()
    }

    fun stop() {
        state = WatchState.IDLE
        invokeChangeListener()
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

    fun pauseResumeAction(): NotificationCompat.Action {
        val text = if (state == WatchState.PAUSED) R.string.resume else R.string.pause
        return getAction(text, ACTION_PAUSE_RESUME, 5)
    }

    fun getAction(
        @StringRes stringRes: Int,
        action: String,
        requestCode: Int
    ) = NotificationCompat.Action.Builder(
        null,
        getString(stringRes),
        getPendingIntent(action, requestCode)
    ).build()

    private fun getPendingIntent(
        action: String,
        requestCode: Int
    ): PendingIntent = PendingIntent.getBroadcast(
        this,
        requestCode,
        Intent(UPDATE_STATE_ACTION)
            .putExtra(ACTION_EXTRA_KEY, action),
        PendingIntent.FLAG_IMMUTABLE
    )

    override fun onDestroy() {
        runCatching {
            unregisterReceiver(receiver)
        }
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

    companion object {
        const val UPDATE_STATE_ACTION = "com.bnyro.clock.UPDATE_STATE"
        const val ACTION_EXTRA_KEY = "action"
        const val ACTION_PAUSE_RESUME = "pause_resume"
        const val ACTION_STOP = "stop"
    }
}
