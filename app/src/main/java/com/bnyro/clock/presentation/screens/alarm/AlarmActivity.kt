package com.bnyro.clock.presentation.screens.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.bnyro.clock.App
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.services.AlarmService
import kotlinx.coroutines.runBlocking


class AlarmActivity : ComponentActivity() {
    private var alarm by mutableStateOf(Alarm(0, 0))

    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val gravitySensor: Sensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) as Sensor
    }
    private var facingDownInitially: Boolean? = null

    private val closeAlertReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getStringExtra(ACTION_EXTRA_KEY) == CLOSE_ACTION) {
                finish()
            }
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
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        ContextCompat.registerReceiver(
            this, closeAlertReciever, IntentFilter(
                ALARM_ALERT_CLOSE_ACTION
            ), ContextCompat.RECEIVER_NOT_EXPORTED
        )

        window.addFlags(windowFlags)
        enableEdgeToEdge()
        setContent {
            AlarmAlertScreen(
                onDismiss = this@AlarmActivity::dismiss,
                onSnooze = this@AlarmActivity::snooze,
                label = alarm.label,
                snoozeEnabled = alarm.snoozeEnabled,
                snoozeTime = alarm.snoozeMinutes
            )
        }

        handleIntent(intent)
    }

    private fun dismiss() {
        stopService(
            Intent(
                this@AlarmActivity.applicationContext,
                AlarmService::class.java
            )
        )
        this@AlarmActivity.finish()
    }

    private fun snooze(minutes: Int = alarm.snoozeMinutes) {
        stopService(
            Intent(
                this@AlarmActivity.applicationContext,
                AlarmService::class.java
            )
        )
        AlarmHelper.snooze(this@AlarmActivity, alarm, minutes)
        this@AlarmActivity.finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            snooze()
        }
        return true
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        val alarmRepository = (application as App).container.alarmRepository
        this.alarm = runBlocking {
            alarmRepository.getAlarmById(id)
        } ?: return
    }

    override fun onDestroy() {
        unregisterReceiver(closeAlertReciever)
        super.onDestroy()
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

    companion object {
        const val ALARM_ALERT_CLOSE_ACTION = "com.bnyro.clock.ALARM_ALERT_CLOSE_ACTION"
        const val ACTION_EXTRA_KEY = "action"
        const val CLOSE_ACTION = "CLOSE"
        private const val windowFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    }
}
