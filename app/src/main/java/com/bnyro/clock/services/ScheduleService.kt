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
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.clock.R
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.obj.WatchState
import java.util.Timer
import java.util.TimerTask

abstract class ScheduleService : Service() {
    abstract val notificationId: Int
    private val binder = LocalBinder()
    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    var scheduledObjects = mutableListOf<ScheduledObject>()
    val updateDelay = 10

    var changeListener: (objects: List<ScheduledObject>) -> Unit = {}

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("receive", intent.toString())
            val id = intent.getIntExtra(ID_EXTRA_KEY, 0)
            val obj = scheduledObjects.find { it.id == id } ?: return
            when (intent.getStringExtra(ACTION_EXTRA_KEY)) {
                ACTION_STOP -> stop(obj)
                ACTION_PAUSE_RESUME -> {
                    if (obj.state.value == WatchState.PAUSED) resume(obj) else pause(obj)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, getStartNotification())
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

    fun enqueueNew(scheduledObject: ScheduledObject) {
        scheduledObject.state.value = WatchState.RUNNING
        scheduledObjects.add(scheduledObject)
        invokeChangeListener()
        updateNotification(scheduledObject)
    }

    fun invokeChangeListener() {
        changeListener.invoke(scheduledObjects)
    }

    fun pause(scheduledObject: ScheduledObject) {
        scheduledObject.state.value = WatchState.PAUSED
        invokeChangeListener()
        updateNotification(scheduledObject)
    }

    fun resume(scheduledObject: ScheduledObject) {
        scheduledObject.state.value = WatchState.RUNNING
        invokeChangeListener()
        updateNotification(scheduledObject)
    }

    fun stop(scheduledObject: ScheduledObject) {
        scheduledObjects.removeAll { it.id == scheduledObject.id }
        NotificationManagerCompat.from(this).cancel(scheduledObject.id)
        invokeChangeListener()
        if (scheduledObjects.isEmpty()) onDestroy()
    }

    abstract fun updateState()

    fun updateNotification(scheduledObject: ScheduledObject) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(scheduledObject.id, getNotification(scheduledObject))
        }
    }

    abstract fun getNotification(scheduledObject: ScheduledObject): Notification

    abstract fun getStartNotification(): Notification

    fun pauseResumeAction(scheduledObject: ScheduledObject): NotificationCompat.Action {
        val text = if (scheduledObject.state.value == WatchState.PAUSED) R.string.resume else R.string.pause
        return getAction(text, ACTION_PAUSE_RESUME, 5, scheduledObject.id)
    }

    fun stopAction(scheduledObject: ScheduledObject) = getAction(
        R.string.stop,
        ACTION_STOP,
        4,
        scheduledObject.id
    )

    private fun getAction(
        @StringRes stringRes: Int,
        action: String,
        requestCode: Int,
        objectId: Int
    ): NotificationCompat.Action {
        val intent = Intent(UPDATE_STATE_ACTION)
            .putExtra(ACTION_EXTRA_KEY, action)
            .putExtra(ID_EXTRA_KEY, objectId)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode + objectId,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Action.Builder(null, getString(stringRes), pendingIntent).build()
    }

    override fun onDestroy() {
        runCatching {
            unregisterReceiver(receiver)
        }
        timer.cancel()
        changeListener = {}
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
        const val ID_EXTRA_KEY = "id"
        const val ACTION_PAUSE_RESUME = "pause_resume"
        const val ACTION_STOP = "stop"
    }
}
