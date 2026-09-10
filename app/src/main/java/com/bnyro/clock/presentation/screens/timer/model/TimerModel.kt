package com.bnyro.clock.presentation.screens.timer.model

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.clock.domain.model.TimerDescriptor
import com.bnyro.clock.domain.model.TimerObject
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.util.services.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerModel : ViewModel() {
    val _timerObjects = MutableStateFlow(emptyList<TimerObject>())
    val scheduledObjects = _timerObjects.asStateFlow()

    var onEnqueue: ((timer: TimerObject) -> Unit)? = null
    var updateTimer: (id: Int, settings: TimerSettings) -> Unit = { _, _ -> }

    var savedTimers by mutableStateOf(
        TimerSettings.getSavedTimers(),
        policy = object : SnapshotMutationPolicy<List<TimerSettings>> {
            override fun equivalent(a: List<TimerSettings>, b: List<TimerSettings>): Boolean {
                if (a == b) return true
                TimerSettings.setSavedTimers(b)
                return false
            }
        }
    )

    var timePickerSeconds by mutableStateOf(60)

    fun onChangeTimers(objects: Array<TimerObject>) {
        _timerObjects.value = listOf(*objects)
    }

    fun removeSavedTimer(id: Int) {
        savedTimers = savedTimers.filter { it.id != id }
    }

    fun addSavedTimer(seconds: Int) {
        if (seconds == 0) return
        val newTimer = TimerSettings(seconds = seconds)
        if (savedTimers.any { it.copy(id = 0) == newTimer }) return
        savedTimers = savedTimers + newTimer.copy(
            id = (savedTimers.maxOfOrNull { it.id } ?: 0) + 1
        )
    }

    fun copySavedTimer(timer: TimerSettings) {
        savedTimers = savedTimers + timer.copy(
            id = (savedTimers.maxOfOrNull { it.id } ?: 0) + 1
        )
    }

    fun updateSavedTimer(settings: TimerSettings) {
        savedTimers = savedTimers.map { if (it.id == settings.id) settings else it }
    }

    fun startTimer(context: Context, settings: TimerSettings) {
        if (settings.seconds == 0) return

        val newTimer = TimerDescriptor(
            // id randomized by system current time; used modulo to compensate for integer overflow
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            settings = settings
        )

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

    fun pauseResumeTimer(context: Context, id: Int) {
        context.sendBroadcast(TimerService.updateStateIntent(TimerService.ACTION_PAUSE_RESUME, id))
    }

    fun stopTimer(context: Context, id: Int) {
        context.sendBroadcast(TimerService.updateStateIntent(TimerService.ACTION_STOP, id))
    }

    fun addTimeToTimer(context: Context, id: Int) {
        context.sendBroadcast(TimerService.updateStateIntent(TimerService.ACTION_ADD_TIME, id))
    }

    fun restartTimer(context: Context, id: Int) {
        context.sendBroadcast(TimerService.updateStateIntent(TimerService.TIMER_RESTART, id))
    }
}
