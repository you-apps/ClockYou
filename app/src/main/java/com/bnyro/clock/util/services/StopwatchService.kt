package com.bnyro.clock.util.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.ui.MainActivity
import com.bnyro.clock.util.NotificationHelper
import java.util.Timer
import java.util.TimerTask

class StopwatchService : Service() {
    private val notificationId = 1
    var currentPosition = 0L
        private set
    var state = WatchState.IDLE
        private set

    private val timer = Timer()

    private lateinit var contentIntent: PendingIntent
    private lateinit var notificationManager: NotificationManagerCompat
    private var notificationPermission = true

    var onPositionChange: (Long) -> Unit = {}
    var onStateChange: (WatchState) -> Unit = {}

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extra = intent.getStringExtra(ACTION_EXTRA_KEY)
            Log.d("Stopwatch Actions", extra.toString())
            when (extra) {
                ACTION_START -> start()
                ACTION_STOP -> stop()
                ACTION_PAUSE_RESUME -> {
                    if (state == WatchState.PAUSED) resume() else pause()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)

        contentIntent = PendingIntent.getActivity(
            this,
            8,
            Intent(this, MainActivity::class.java).setAction(MainActivity.SHOW_STOPWATCH_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        ContextCompat.registerReceiver(this, receiver, IntentFilter(STOPWATCH_INTENT_ACTION), ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        counterTask?.cancel()
        counterTask = null
        timer.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, getNotification())
        return START_STICKY
    }

    private var counterTask: TimerTask? = null

    private fun start() {
        currentPosition = 0
        updateState(WatchState.RUNNING)
        counterTask = object : TimerTask() {
            override fun run() {
                if (state != WatchState.PAUSED) {
                    currentPosition += UPDATE_DELAY
                    onPositionChange(currentPosition)
                }
            }
        }
        timer.schedule(counterTask, 0, UPDATE_DELAY.toLong())
    }

    private fun stop() {
        updateState(WatchState.IDLE)
        notificationManager.cancel(notificationId)
        currentPosition = 0
        onPositionChange(currentPosition)
        counterTask?.cancel()
        counterTask = null
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification() {
        if (notificationPermission) {
            notificationManager.notify(notificationId, getNotification())
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getNotification(): Notification {
        return NotificationCompat.Builder(
            this,
            NotificationHelper.STOPWATCH_CHANNEL
        )
            .setContentTitle(getText(R.string.stopwatch))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .apply {
                if (state != WatchState.IDLE) {
                    addAction(stopAction())
                    addAction(pauseResumeAction())
                }
            }
            .setUsesChronometer(state == WatchState.RUNNING)
            .setWhen(System.currentTimeMillis() - currentPosition)
            .build()
    }

    private fun pause() {
        updateState(WatchState.PAUSED)
    }

    private fun resume() {
        updateState(WatchState.RUNNING)
    }

    private fun updateState(newState: WatchState) {
        state = newState
        updateNotification()
        onStateChange(newState)
    }

    private fun getAction(title: String, requestCode: Int, action: String) =
        NotificationCompat.Action.Builder(
            null,
            title,
            getPendingIntent(
                Intent(STOPWATCH_INTENT_ACTION).putExtra(
                    ACTION_EXTRA_KEY,
                    action
                ), requestCode
            )
        ).build()

    private fun stopAction() =
        getAction(getString(R.string.stop), STOP_ACTION_REQUEST_CODE, ACTION_STOP)

    private fun pauseResumeAction(): NotificationCompat.Action {
        val text =
            if (state == WatchState.RUNNING) getString(R.string.pause) else getString(R.string.resume)
        return getAction(
            text,
            PAUSE_RESUME_ACTION_REQUEST_CODE,
            ACTION_PAUSE_RESUME
        )
    }

    private fun getPendingIntent(intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private val binder = LocalBinder()
    override fun onBind(intent: Intent) = binder

    inner class LocalBinder : Binder() {
        fun getService() = this@StopwatchService
    }

    companion object {
        private const val UPDATE_DELAY = 10

        const val STOPWATCH_INTENT_ACTION = "com.bnyro.clock.STOPWATCH_ACTION"
        const val ACTION_EXTRA_KEY = "action"
        const val ACTION_PAUSE_RESUME = "pause_resume"
        const val ACTION_STOP = "stop"
        const val ACTION_START = "start"

        const val STOP_ACTION_REQUEST_CODE = 6
        const val PAUSE_RESUME_ACTION_REQUEST_CODE = 7
    }
}
