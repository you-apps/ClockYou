package com.bnyro.clock.presentation.screens.ringing

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.bnyro.clock.domain.model.VolumeButtonAction
import com.bnyro.clock.util.Preferences

/**
 * The screen an alarm or a timer takes over the phone with while it rings: it shows over the lock
 * screen, wakes the display, keeps it awake, and answers the phone put face down or the volume keys
 * the way the reader asked for. What it rings for, and what dismissing or snoozing it means, belongs
 * to whoever it is showing.
 */
abstract class RingingActivity : ComponentActivity() {
    /** The broadcast the service sends once the thing being shown has stopped ringing. */
    protected abstract val closeAction: String

    /** The preference naming what the volume keys do while this screen is up. */
    protected abstract val volumeButtonActionKey: String

    /** What the volume keys do while that preference has never been set. */
    protected open val volumeButtonActionDefault = VolumeButtonAction.SNOOZE

    protected abstract fun dismiss()

    protected abstract fun snooze()

    /** Whether snoozing is offered at all, which the volume keys fall back from when it is not. */
    protected open val snoozeAvailable: Boolean get() = true

    /** Whether a close broadcast is about what this screen is currently showing. */
    protected open fun closesThisAlert(intent: Intent) = true

    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val gravitySensor: Sensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) as Sensor
    }
    private var facingDownInitially: Boolean? = null

    private val closeAlertReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getStringExtra(ACTION_EXTRA_KEY) != CLOSE_ACTION) return
            if (closesThisAlert(intent)) finish()
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val gravityThreshold = SensorManager.GRAVITY_EARTH * 0.95f

            if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                val isDown = event.values[2] < -gravityThreshold
                if (facingDownInitially == null) {
                    facingDownInitially = isDown
                    return
                }
                if (isDown && facingDownInitially != true) {
                    dismiss()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_ALARM

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        ContextCompat.registerReceiver(
            this,
            closeAlertReciever,
            IntentFilter(closeAction),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        enableEdgeToEdge()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP) {
            return super.onKeyDown(keyCode, event)
        }

        return when (
            VolumeButtonAction.valueOf(
                Preferences.instance.getString(
                    volumeButtonActionKey,
                    volumeButtonActionDefault.name
                ) ?: volumeButtonActionDefault.name
            )
        ) {
            VolumeButtonAction.SNOOZE -> {
                if (snoozeAvailable) snooze() else dismiss()
                true
            }
            VolumeButtonAction.DISMISS -> {
                dismiss()
                true
            }
            VolumeButtonAction.CONTROL_VOLUME -> super.onKeyDown(keyCode, event)
            VolumeButtonAction.DO_NOTHING -> true
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            sensorEventListener,
            gravitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        unregisterReceiver(closeAlertReciever)
        super.onDestroy()
    }

    companion object {
        const val ACTION_EXTRA_KEY = "action"
        const val CLOSE_ACTION = "CLOSE"
    }
}
