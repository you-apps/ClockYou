package com.bnyro.clock.util.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.format.DateUtils
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.TimerDescriptor
import com.bnyro.clock.domain.model.TimerObject
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.util.NotificationHelper
import com.bnyro.clock.util.RingtoneHelper

import java.util.Timer
import java.util.TimerTask

class TimerService : Service() {
    private val notificationId = 2
    private val timer = Timer()
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())

    var onChangeTimers: (objects: Array<TimerObject>) -> Unit = {}

    var timerObjects = mutableListOf<TimerObject>()







    @SuppressLint("ServiceCast", "ScheduleExactAlarm")

    private fun scheduleAlarm(timerObject: TimerObject) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, TimerAlarmReceiver::class.java).apply {
            putExtra(ID_EXTRA_KEY, timerObject.id)
            action = ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            timerObject.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = System.currentTimeMillis() + timerObject.currentPosition.value

        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)

        try {
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.e("TimerService", "timer error!", e)
        }
    }
    private fun cancelAlarm(timerObject: TimerObject) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TimerAlarmReceiver::class.java).apply {
            action = ACTION_TIMER_EXPIRED
            putExtra(ID_EXTRA_KEY, timerObject.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            timerObject.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }













  //april fools 2026 O:








    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("receive", intent.toString())
            val id = intent.getIntExtra(ID_EXTRA_KEY, 0)
            val obj = timerObjects.find { it.id == id } ?: return
            when (intent.getStringExtra(ACTION_EXTRA_KEY)) {
                ACTION_STOP -> {
                    stop(obj, cancelled = true)
                    stopForeground(true)



                }
                ACTION_PAUSE_RESUME -> {
                    if (obj.state.value == WatchState.PAUSED) resume(obj) else pause(obj)
                }
                ACTION_ADD_5_MIN -> {
                    obj.currentPosition.value += 300000
                    updateNotification(obj)
                }
                TIMER_RESTART -> {
                    obj.currentPosition.value = obj.initialPosition

                    cancelAlarm(obj)
                    if (obj.state.value == WatchState.RUNNING) {
                        scheduleAlarm(obj)
                    }

                    updateNotification(obj)

                    //the android api alarm doesnt restart with this!!!!! DANGER D:::: OH NOES WERE GONNA DIEEEEEEEEE D: ARGHHHH
                    // it does now were saveddddddddddddddddddddddddddddd.... hold up the pause gotta be bugged D:, noooooooooooooooooooooooooooooooooooooooooooooooooooooooo AAGHHHHHHHHHHHHH ARGHHHh
                    // it i snt even TWT




                }
            }
        }
    }






    private var wakeLock: PowerManager.WakeLock? = null
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()

        //maybe keeps the phone on so timer works? pls pls pls
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TimerService::Lock").apply {
            acquire()
        }




        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    handler.post(this@TimerService::updateState)
                }
            },
            0,
            UPDATE_DELAY.toLong()
        )
        ContextCompat.registerReceiver(
            this, receiver,
            IntentFilter(UPDATE_STATE_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_TIMER_EXPIRED) {
            val id = intent.getIntExtra(ID_EXTRA_KEY, 0)
            timerObjects.find { it.id == id }?.let {
                showFinishedNotification(it)
                stop(it, cancelled = false)
            }
            return START_STICKY
        }
        val timer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(INITIAL_TIMER_EXTRA_KEY, TimerDescriptor::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(INITIAL_TIMER_EXTRA_KEY) as TimerDescriptor?
        }
        if (timer != null) {
            val scheduledObject = timer.asScheduledObject()
            startForeground(scheduledObject.id, getStartNotification())
            enqueueNew(scheduledObject)
        } else {
            startForeground(notificationId, getStartNotification())
        }
        return START_STICKY
    }

    private fun getNotification(timerObject: TimerObject) = NotificationCompat.Builder(
        this,
        NotificationHelper.TIMER_CHANNEL
    )
        .setContentTitle(getText(R.string.timer))
        .setUsesChronometer(timerObject.state.value == WatchState.RUNNING)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(true)
            } else {
                setContentText(
                    DateUtils.formatElapsedTime(
                        (timerObject.currentPosition.value / 1000).toLong()
                    )
                )
            }
        }
        .setWhen(System.currentTimeMillis() + timerObject.currentPosition.value)
        .addAction(stopAction(timerObject))
        .addAction(pauseResumeAction(timerObject))
        .addAction(restarttimer(timerObject))
        .addAction(add5MinAction(timerObject))

        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .build()

    fun invokeChangeListener() {
        onChangeTimers.invoke(timerObjects.toTypedArray())
    }

    private fun updateState() {
        timerObjects.forEach {
            if (it.state.value == WatchState.RUNNING) {

                it.currentPosition.value -= UPDATE_DELAY


                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    updateNotification(it)
                }
            }

        }
    }

    fun enqueueNew(timerObject: TimerObject) {
        timerObject.state.value = WatchState.RUNNING
        timerObjects.add(timerObject)

        scheduleAlarm(timerObject)

        invokeChangeListener()
        updateNotification(timerObject)
    }


    private fun pause(timerObject: TimerObject) {
        timerObject.state.value = WatchState.PAUSED
        cancelAlarm(timerObject)
        updateNotification(timerObject)

    }

    private fun resume(timerObject: TimerObject) {
        timerObject.state.value = WatchState.RUNNING
        scheduleAlarm(timerObject)
        updateNotification(timerObject)
    }

    private fun updateNotification(timerObject: TimerObject) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(timerObject.id, getNotification(timerObject))
        }
    }

    private fun stop(timerObject: TimerObject, cancelled: Boolean) {
        cancelAlarm(timerObject) //hopefully cancels that shit?



        timerObjects.remove(timerObject)

        invokeChangeListener()
        if (cancelled) {
            NotificationManagerCompat.from(this)
                .cancel(timerObject.id)


        }
        if (timerObjects.isEmpty()) stopSelf()
    }


    private fun showFinishedNotification(timerObject: TimerObject) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val ringtoneUri = timerObject.ringtone ?: RingtoneHelper().getDefault(this)
        val vibrationPattern = NotificationHelper.vibrationPattern.takeIf { timerObject.vibrate }
        val notificationChannelId =
            NotificationHelper.TIMER_FINISHED_CHANNEL + "-" + System.currentTimeMillis()
        val notificationId = (Integer.MAX_VALUE / 3) + timerObject.id * 10
        // create a new temporary notification channel in order to work around the restriction
        // that apps can only set the ringtone uri and vibration pattern upon notification channel
        // creation, but can't update it
        val deleteNotificationChannelIntent = NotificationHelper.createDynamicChannel(
            this,
            R.string.timer_finished,
            notificationChannelId,
            ringtoneUri = ringtoneUri,
            vibrationPattern = vibrationPattern
        )
        val deleteNotificationChannelPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 1,
            deleteNotificationChannelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        cancelAlarm(timerObject)  //maybe here ???


        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(ringtoneUri)
            .setVibrate(vibrationPattern)
            .setContentTitle(getString(R.string.timer_finished))
            .setContentText(timerObject.label.value)
            .setDeleteIntent(deleteNotificationChannelPendingIntent)
            .build().apply {
                flags = flags or NotificationCompat.FLAG_INSISTENT
            }

        NotificationManagerCompat.from(this)
            .notify(notificationId, notification)
    }

    private fun pauseResumeAction(timerObject: TimerObject): NotificationCompat.Action {
        val text =
            if (timerObject.state.value == WatchState.PAUSED) R.string.resume else R.string.pause
        return getAction(text, ACTION_PAUSE_RESUME, 5, timerObject.id)
    }

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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(null, getString(stringRes), pendingIntent).build()
    }

    private fun stopAction(timerObject: TimerObject) = getAction(
        R.string.stop,
        ACTION_STOP,
        4,
        timerObject.id
    )
    private fun restarttimer(timerObject: TimerObject) = getAction(
        R.string.timer_restart,
        TIMER_RESTART,
        7,
        timerObject.id
    )
    private fun add5MinAction(timerObject: TimerObject) = getAction(
        R.string.add_5_minutes,
        ACTION_ADD_5_MIN,
        6,
        timerObject.id
    )

    fun updateLabel(id: Int, newLabel: String) {
        timerObjects.firstOrNull { it.id == id }?.let {
            it.label.value = newLabel
        }
    }

    fun updateRingtone(id: Int, newRingtoneUri: Uri?) {
        timerObjects.firstOrNull { it.id == id }?.let {
            it.ringtone = newRingtoneUri
        }
    }

    fun updateVibrate(id: Int, vibrate: Boolean) {
        timerObjects.firstOrNull { it.id == id }?.let {
            it.vibrate = vibrate
        }
    }

    override fun onDestroy() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        runCatching {
            unregisterReceiver(receiver)
        }
        timer.cancel()
        super.onDestroy()
    }

    private fun getStartNotification() = NotificationCompat.Builder(
        this,
        NotificationHelper.TIMER_SERVICE_CHANNEL
    )
        .setContentTitle(getString(R.string.timer_service))
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    override fun onBind(intent: Intent) = binder

    inner class LocalBinder : Binder() {
        fun getService() = this@TimerService
    }


























































    companion object {
        const val UPDATE_STATE_ACTION = "com.bnyro.clock.UPDATE_STATE"
        const val ACTION_EXTRA_KEY = "action"
        const val ID_EXTRA_KEY = "id"
        const val INITIAL_TIMER_EXTRA_KEY = "timer"
        const val ACTION_PAUSE_RESUME = "pause_resume"
        const val ACTION_STOP = "stop"
        private const val UPDATE_DELAY = 100
        const val TIMER_RESTART = "timer_restart"
        const val ACTION_ADD_5_MIN = "add_5_min"
        const val ACTION_TIMER_EXPIRED = "com.bnyro.clock.TIMER_EXPIRED"
    }
}