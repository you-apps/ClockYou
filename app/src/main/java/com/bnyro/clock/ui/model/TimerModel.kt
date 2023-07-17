package com.bnyro.clock.ui.model

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.services.ScheduleService
import com.bnyro.clock.services.TimerService

const val INITIAL_SECONDS_STATE = "0"

class TimerModel : ViewModel() {
    var scheduledObjects = mutableStateListOf<ScheduledObject>()
    var timePickerSecondsState by mutableStateOf(INITIAL_SECONDS_STATE)
    private var objectToEnqueue: ScheduledObject? = null

    private fun getTotalSeconds(): Int {
        val timerDelay = timePickerSecondsState.toInt()
        val seconds = timerDelay % 100
        val minutes = (timerDelay - seconds) / 100 % 100
        val hours = (timerDelay - seconds - minutes * 100) / 10000 % 100

        return seconds + minutes * 60 + hours * 3600
    }

    fun getHours() = timePickerSecondsState.toInt() / 10000

    fun getMinutes() = (timePickerSecondsState.toInt() - getHours() * 10000) / 100

    fun getSeconds() = timePickerSecondsState.toInt() % 100

    // Ensures that the seconds state has 6 digits
    private fun getSecondsStringPadded() = timePickerSecondsState.padStart(6, '0')

    @SuppressLint("StaticFieldLeak")
    var service: TimerService? = null

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
        val totalSeconds = delay ?: getTotalSeconds()
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

        timePickerSecondsState = INITIAL_SECONDS_STATE

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

    fun addNumber(number: String) {
        // Adding 0 to 0 makes no sense
        if (number.toIntOrNull() == 0 && timePickerSecondsState == "0") return

        // Couldn't find a better way to substring
        timePickerSecondsState = (timePickerSecondsState + number)
            .padEnd(7, 'x')
            .substring(0, 7)
            .replace("x", "")
    }

    fun addSeconds(seconds: Int) {
        timePickerSecondsState = getSecondsStringPadded().substring(0, 4) +
            seconds.toString().padStart(2, '0')
    }

    fun addMinutes(minutes: Int) {
        timePickerSecondsState = getSecondsStringPadded().substring(0, 2) +
            minutes.toString().padStart(2, '0') +
            getSecondsStringPadded().substring(4)
    }

    fun addHours(hours: Int) {
        timePickerSecondsState = hours.toString().padStart(2, '0') +
            getSecondsStringPadded().substring(2)
    }

    fun deleteLastNumber() {
        timePickerSecondsState = timePickerSecondsState.dropLast(1).ifEmpty { "0" }
    }

    fun clear() {
        timePickerSecondsState = INITIAL_SECONDS_STATE
    }

    fun setSeconds(seconds: Int) {
        val remainingSeconds = seconds % 60
        val minutes = (seconds - remainingSeconds) / 60
        val remainingMinutes = minutes % 60
        val hours = (minutes - remainingMinutes) / 60
        val remainingHours = hours % 24

        timePickerSecondsState = INITIAL_SECONDS_STATE

        addSeconds(remainingSeconds)
        addMinutes(remainingMinutes)
        addHours(remainingHours)
    }
}
