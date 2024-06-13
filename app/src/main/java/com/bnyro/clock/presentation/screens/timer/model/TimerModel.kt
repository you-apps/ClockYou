package com.bnyro.clock.presentation.screens.timer.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.domain.model.PersistentTimer
import com.bnyro.clock.domain.model.TimerDescriptor
import com.bnyro.clock.domain.model.TimerObject
import com.bnyro.clock.util.services.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerModel : ViewModel() {
    val _timerObjects = MutableStateFlow(emptyList<TimerObject>())
    val scheduledObjects = _timerObjects.asStateFlow()

    var onEnqueue: ((timer: TimerObject) -> Unit)? = null
    var updateLabel: (id: Int, newLabel: String) -> Unit = { _, _ -> }
    var updateRingtone: (id: Int, newRingtoneUri: Uri?) -> Unit = { _, _ -> }
    var updateVibrate: (id: Int, vibrate: Boolean) -> Unit = { _, _ -> }

    var persistentTimers by mutableStateOf(
        PersistentTimer.getTimers(),
        policy = object : SnapshotMutationPolicy<List<PersistentTimer>> {
            override fun equivalent(a: List<PersistentTimer>, b: List<PersistentTimer>): Boolean {
                if (a == b) return true
                PersistentTimer.setTimers(b)
                return false
            }
        }
    )

    var timePickerSeconds = 0
    var hours
        get() = timePickerSeconds / 3600
        set(value) {
            timePickerSeconds += (value - hours) * 3600
        }
    var minutes
        get() = (timePickerSeconds % 3600) / 60
        set(value) {
            timePickerSeconds += (value - minutes) * 60
        }
    var seconds
        get() = (timePickerSeconds % 3600) % 60
        set(value) {
            timePickerSeconds += (value - seconds)
        }


    fun onChangeTimers(objects: Array<TimerObject>) {
        _timerObjects.value = listOf(*objects)
    }

    fun removePersistentTimer(index: Int) {
        persistentTimers = persistentTimers.filterIndexed { i, _ -> i != index }
    }

    fun addPersistentTimer(seconds: Int) {
        if (seconds == 0) return
        persistentTimers = (persistentTimers + PersistentTimer(seconds)).distinct()
    }

    fun startTimer(context: Context, delay: Int? = null) {
        val totalSeconds = delay ?: timePickerSeconds
        if (totalSeconds == 0) return

        val newTimer = TimerDescriptor(
            // id randomized by system current time; used modulo to compensate for integer overflow
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            currentPosition = totalSeconds * 1000
        )

        timePickerSeconds = 0
        timePickerFakeUnits = 0

        if (_timerObjects.value.isEmpty()) {
            startService(context, newTimer)
        } else {
            onEnqueue?.invoke(newTimer.asScheduledObject())
        }
    }

    private fun startService(context: Context, timerDescriptor: TimerDescriptor) {
        val intent = Intent(context, TimerService::class.java)
            .putExtra(TimerService.INITIAL_TIMER_EXTRA_KEY, timerDescriptor)
        context.startService(intent)
    }

    fun pauseResumeTimer(context: Context, index: Int) {
        val pauseResumeIntent = Intent(TimerService.UPDATE_STATE_ACTION)
            .putExtra(
                TimerService.ID_EXTRA_KEY,
                index
            )
            .putExtra(
                TimerService.ACTION_EXTRA_KEY,
                TimerService.ACTION_PAUSE_RESUME
            )
        context.sendBroadcast(pauseResumeIntent)
    }

    fun stopTimer(context: Context, index: Int) {
        val stopIntent = Intent(TimerService.UPDATE_STATE_ACTION)
            .putExtra(
                TimerService.ID_EXTRA_KEY,
                index
            )
            .putExtra(
                TimerService.ACTION_EXTRA_KEY,
                TimerService.ACTION_STOP
            )
        context.sendBroadcast(stopIntent)
    }

    /* =============== Numpad time picker ======================== */
    var timePickerFakeUnits by mutableStateOf(
        0,
        policy = object : SnapshotMutationPolicy<Int> {
            override fun equivalent(a: Int, b: Int): Boolean {
                if (a == b) return true
                b.let {
                    val roughHours = (it / 10000) % 100
                    val roughMinutes = (it / 100) % 100
                    val roughSeconds = it % 100
                    timePickerSeconds =
                        roughSeconds + (roughMinutes * 60) + (roughHours * 3600)
                }
                return false
            }
        }
    )

    fun addNumber(number: String) {
        // don't do anything if all necessary/possible numbers have been entered already
        if (hours >= 10) return

        if (number == "00") {
            timePickerFakeUnits *= 100
            return
        }
        timePickerFakeUnits = (timePickerFakeUnits * 10) + number.toInt()
    }

    fun deleteLastNumber() {
        timePickerFakeUnits /= 10
    }

    fun clear() {
        timePickerFakeUnits = 0
    }
    /* ========================================================== */
}
