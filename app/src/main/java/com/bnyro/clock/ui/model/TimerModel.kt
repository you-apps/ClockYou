package com.bnyro.clock.ui.model

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.services.ScheduleService
import com.bnyro.clock.services.TimerService

class TimerModel : ViewModel() {
    var scheduledObjects = mutableStateListOf<ScheduledObject>()
    private var objectToEnqueue: ScheduledObject? = null

    @SuppressLint("StaticFieldLeak")
    var service: TimerService? = null

    var timePickerSeconds = 0
    var hours
        get() = timePickerSeconds.div(3600)
        set(value) {
            timePickerSeconds += (value - hours) * 3600
        }
    var minutes
        get() = timePickerSeconds.mod(3600).div(60)
        set(value) {
            timePickerSeconds += (value - minutes) * 60
        }
    var seconds
        get() = timePickerSeconds.mod(3600).mod(60)
        set(value) {
            timePickerSeconds += (value - seconds)
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName, binder: IBinder) {
            service = (binder as ScheduleService.LocalBinder).getService() as? TimerService
            service?.changeListener = { objects ->
                this@TimerModel.scheduledObjects.clear()
                this@TimerModel.scheduledObjects.addAll(objects)
            }
            objectToEnqueue?.let { service?.enqueueNew(it) }
            objectToEnqueue = null
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            scheduledObjects.clear()
            service = null
        }
    }

    fun startTimer(context: Context, delay: Int? = null) {
        val totalSeconds = delay ?: timePickerSeconds
        if (totalSeconds == 0) return

        if (scheduledObjects.isEmpty()) {
            runCatching {
                context.unbindService(serviceConnection)
            }
            service = null
        }

        val newTimer = ScheduledObject(
            label = mutableStateOf(null),
            id = System.currentTimeMillis().toInt(),
            currentPosition = mutableStateOf(totalSeconds * 1000)
        )

        timePickerSeconds = 0
        timePickerFakeUnits = 0

        if (service == null) {
            startService(context)
            objectToEnqueue = newTimer
        } else {
            service?.enqueueNew(newTimer)
        }
    }

    private fun startService(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        runCatching {
            context.stopService(intent)
        }
        runCatching {
            context.unbindService(serviceConnection)
        }
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun tryConnect(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
    }

    fun pauseTimer(index: Int) {
        service?.pause(scheduledObjects[index])
    }

    fun resumeTimer(index: Int) {
        service?.resume(scheduledObjects[index])
    }

    fun stopTimer(context: Context, index: Int) {
        val obj = scheduledObjects[index]
        scheduledObjects.removeAt(index)
        service?.stop(obj)
        if (scheduledObjects.isEmpty()) context.unbindService(serviceConnection)
    }

    /* =============== Numpad time picker ======================== */
    var timePickerFakeUnits by mutableStateOf(
        0,
        policy = object : SnapshotMutationPolicy<Int> {
            override fun equivalent(a: Int, b: Int): Boolean {
                if (a == b) return true
                b.let {
                    val roughHours = it.div(10000).mod(100)
                    val roughMinutes = it.div(100).mod(100)
                    val roughSeconds = it.mod(100)
                    timePickerSeconds =
                        roughSeconds + roughMinutes.times(60) + roughHours.times(3600)
                }
                return false
            }
        }
    )

    fun addNumber(number: String) {
        timePickerFakeUnits = timePickerFakeUnits.times(10).plus(number.toInt())
    }

    fun deleteLastNumber() {
        timePickerFakeUnits = timePickerFakeUnits.div(10)
    }

    fun clear() {
        timePickerFakeUnits = 0
    }
    /* ========================================================== */
}
