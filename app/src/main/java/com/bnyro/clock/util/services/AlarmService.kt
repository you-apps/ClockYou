package com.bnyro.clock.util.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bnyro.clock.App
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.presentation.screens.alarm.AlarmActivity
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.NotificationHelper
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

class AlarmService : Service() {
    private val notificationId = 5
    private var isPlaying = false
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentAlarm: Alarm? = null

    val timer = Timer()

    private var volume: Float = 0.1f

    private val volumeHandler = Handler(Looper.getMainLooper())
    private val volumeRunnable = object : Runnable {
        override fun run() {
            if (volume < MAX_VOLUME) {
                mediaPlayer!!.setVolume(volume, volume)
                volume += VOLUME_INCREASE_STEP
                volumeHandler.postDelayed(this, VOLUME_INCREASE_INTERVAL)
            }
        }
    }

    private val alarmActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(ACTION_EXTRA_KEY)) {
                DISMISS_ACTION -> stopSelf()
                SNOOZE_ACTION -> {
                    AlarmHelper.snooze(this@AlarmService, currentAlarm!!)
                    stopSelf()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        ContextCompat.registerReceiver(
            this,
            alarmActionReceiver,
            IntentFilter(ALARM_INTENT_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )

        super.onCreate()
    }

    override fun onDestroy() {
        stop()
        timer.cancel()
        unregisterReceiver(alarmActionReceiver)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L }
            ?: return START_STICKY
        val alarmRepository = (application as App).container.alarmRepository
        val alarm = runBlocking {
            alarmRepository.getAlarmById(id)
        } ?: return START_STICKY

        startForeground(notificationId, createNotification(this, alarm))
        play(alarm)
        currentAlarm = alarm
        timer.schedule(object : TimerTask() {
            override fun run() {
                stopSelf()
            }
        }, AUTO_SNOOZE_MINUTES * 60 * 1000L, AUTO_SNOOZE_MINUTES * 60 * 1000L)
        return START_STICKY
    }

    private fun play(alarm: Alarm) {
        // stop() checks to see if we are already playing.
        stop()
        if (alarm.soundEnabled) {
            val alert: Uri? = alarm.soundUri?.toUri() ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setOnErrorListener { mp, _, _ ->
                Log.e("Media Player", "Error occurred while playing audio.")
                mp.stop()
                mp.release()
                mediaPlayer = null
                true
            }
            try {
                mediaPlayer!!.setDataSource(this, alert!!)
                startAlarm(mediaPlayer!!)
            } catch (e: Exception) {
                Log.e("Failed to play ringtone", e.message, e)
            }
        }

        /* Start the vibrator after everything is ok with the media player */
        if (alarm.vibrate) {
            val vibrationPattern =
                alarm.vibrationPattern.map(Int::toLong).toLongArray()
            vibrator!!.vibrate(vibrationPattern, 0)
        } else {
            vibrator!!.cancel()
        }
        isPlaying = true
    }

    private fun startAlarm(player: MediaPlayer) {
        player.isLooping = true
        player.setAudioAttributes(NotificationHelper.audioAttributes)
        player.prepare()
        player.start()

        volumeHandler.post(volumeRunnable)
    }

    /**
     * Stops alarm
     */
    fun stop() {
        if (!isPlaying) return
        isPlaying = false

        volumeHandler.removeCallbacks(volumeRunnable)
        volume = 0.1f

        // Stop audio playing
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        // Stop vibrator
        vibrator?.cancel()
        NotificationManagerCompat.from(this).cancel(notificationId)

        val closeAlarmAlertIntent = Intent(AlarmActivity.ALARM_ALERT_CLOSE_ACTION).apply {
            putExtra(AlarmActivity.ACTION_EXTRA_KEY, AlarmActivity.CLOSE_ACTION)
            `package` = packageName
        }
        sendBroadcast(closeAlarmAlertIntent)
    }

    private fun createNotification(context: Context, alarm: Alarm): Notification {
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            )
            .putExtra(AlarmHelper.EXTRA_ID, alarm.id)
        val pendingIntent = PendingIntent.getActivity(
            this@AlarmService,
            0,
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissAlarmIntent = Intent(ALARM_INTENT_ACTION)
            .putExtra(ACTION_EXTRA_KEY, DISMISS_ACTION)
        val onDeleteIntent = getPendingIntent(dismissAlarmIntent, 1)

        val dismissIntent = Intent(ALARM_INTENT_ACTION).putExtra(ACTION_EXTRA_KEY, DISMISS_ACTION)
        val dismissAction = NotificationCompat.Action.Builder(
            null,
            getString(R.string.dismiss),
            getPendingIntent(dismissIntent, 2)
        )

        val snoozeIntent = Intent(ALARM_INTENT_ACTION).putExtra(ACTION_EXTRA_KEY, SNOOZE_ACTION)
        val snoozeAction = NotificationCompat.Action.Builder(
            null,
            getString(R.string.snooze),
            getPendingIntent(snoozeIntent, 3)
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.ALARM_CHANNEL)
            .apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(alarm.label ?: context.getString(R.string.alarm))
                // setSilent(true)  // This setting causes the full screen intent to not work properly
                setAutoCancel(true)
                priority = NotificationCompat.PRIORITY_MAX
                foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE
                setCategory(NotificationCompat.CATEGORY_ALARM)
                setFullScreenIntent(pendingIntent, true)
                addAction(snoozeAction.build())
                addAction(dismissAction.build())
                setDeleteIntent(onDeleteIntent)
                setOngoing(true)
            }

        return builder.build()
    }

    private fun getPendingIntent(intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        const val ALARM_INTENT_ACTION = "com.bnyro.clock.ALARM_ACTION"
        const val ACTION_EXTRA_KEY = "action"
        const val DISMISS_ACTION = "DISMISS"
        const val SNOOZE_ACTION = "SNOOZE"

        const val AUTO_SNOOZE_MINUTES = 10

        private const val MAX_VOLUME: Float = 1.0f
        private const val VOLUME_INCREASE_STEP: Float = 0.05f
        private const val VOLUME_INCREASE_INTERVAL: Long = 1000
    }
}
