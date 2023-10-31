package com.bnyro.clock.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.bnyro.clock.R
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.ui.AlarmActivity
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.NotificationHelper
import kotlinx.coroutines.runBlocking

class AlarmService : Service() {
    private var isPlaying = false
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentAlarm: Alarm? = null

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        super.onCreate()
    }

    override fun onDestroy() {
        stop()
        Log.d("Alarm Service", "Destroying service")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L }
            ?: return START_STICKY
        val alarm = runBlocking {
            DatabaseHolder.instance.alarmsDao().findById(id)
        }
        startForeground(1, createNotification(this, alarm))
        play(alarm)
        currentAlarm = alarm
        return START_STICKY
    }

    private fun play(alarm: Alarm) {
        // stop() checks to see if we are already playing.
        stop()
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

        /* Start the vibrator after everything is ok with the media player */
        if (alarm.vibrate) {
            vibrator!!.vibrate(NotificationHelper.vibrationPattern, 0)
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
    }

    /**
     * Stops alarm
     */
    fun stop() {
        if (!isPlaying) return
        isPlaying = false

        // Stop audio playing
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        // Stop vibrator
        vibrator?.cancel()
    }

    private fun createNotification(context: Context, alarm: Alarm): Notification {
        NotificationHelper.createAlarmNotificationChannel(context, alarm)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, AlarmActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
                putExtra(AlarmHelper.EXTRA_ID, alarm.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.ALARM_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(alarm.label ?: context.getString(R.string.alarm))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
        return builder.build()
    }
}
