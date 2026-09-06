package com.bnyro.clock.presentation.screens.timer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bnyro.clock.domain.model.VolumeButtonAction
import com.bnyro.clock.presentation.screens.ringing.RingingActivity
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.services.TimerService

class TimerAlertActivity : RingingActivity() {
    private var timerId by mutableIntStateOf(0)
    private var label by mutableStateOf<String?>(null)
    private var ringingSince by mutableLongStateOf(0L)
    private var incrementSeconds by mutableIntStateOf(60)

    override val closeAction = TimerService.TIMER_ALERT_CLOSE_ACTION

    override val volumeButtonActionKey = Preferences.timerVolumeButtonActionKey

    override val volumeButtonActionDefault = VolumeButtonAction.DISMISS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            TimerAlertScreen(
                onDismiss = this@TimerAlertActivity::dismiss,
                onSnooze = this@TimerAlertActivity::snooze,
                onReset = this@TimerAlertActivity::reset,
                label = label,
                ringingSince = ringingSince,
                incrementSeconds = incrementSeconds
            )
        }
    }

    override fun dismiss() = answerWith(TimerService.ACTION_STOP)

    override fun snooze() = answerWith(TimerService.ACTION_ADD_TIME)

    private fun reset() = answerWith(TimerService.TIMER_RESTART)

    private fun answerWith(action: String) {
        sendBroadcast(TimerService.updateStateIntent(action, timerId))
        finish()
    }

    override fun closesThisAlert(intent: Intent) =
        intent.getIntExtra(TimerService.ID_EXTRA_KEY, 0) == timerId

    override fun onStart() {
        super.onStart()
        reportAlert(TimerService.ACTION_ALERT_SHOWN)
    }

    override fun onStop() {
        reportAlert(TimerService.ACTION_ALERT_HIDDEN)
        super.onStop()
    }

    private fun reportAlert(action: String) {
        sendBroadcast(TimerService.updateStateIntent(action, timerId))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        // a takeover hands the showing screen to another timer, which counts as showing it anew
        reportAlert(TimerService.ACTION_ALERT_SHOWN)
    }

    private fun handleIntent(intent: Intent) {
        timerId = intent.getIntExtra(TimerService.ID_EXTRA_KEY, 0)
        label = intent.getStringExtra(TimerService.LABEL_EXTRA_KEY)
        ringingSince = intent.getLongExtra(
            TimerService.RINGING_SINCE_EXTRA_KEY,
            System.currentTimeMillis()
        )
        incrementSeconds = intent.getIntExtra(TimerService.INCREMENT_EXTRA_KEY, 60)
    }
}
