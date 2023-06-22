package com.bnyro.clock.ui.model

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.services.ScheduleService
import com.bnyro.clock.services.TimerService

const val INITIAL_SECONDS_STATE = "000000"

class TimerModel : ViewModel() {
    var state by mutableStateOf(WatchState.IDLE)
    var currentTimeMillis by mutableStateOf(0)
    var timePickerSecondsState by mutableStateOf(INITIAL_SECONDS_STATE)

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

    @SuppressLint("StaticFieldLeak")
    private var service: TimerService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName, binder: IBinder) {
            service = (binder as ScheduleService.LocalBinder).getService() as? TimerService
            service?.changeListener = { state, time ->
                this@TimerModel.state = state
                this@TimerModel.currentTimeMillis = time
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            state = WatchState.IDLE
            service = null
        }
    }

    fun startTimer(context: Context, delay: Int? = null) {
        val totalSeconds = delay ?: getTotalSeconds()
        if (totalSeconds == 0) return

        timePickerSecondsState = INITIAL_SECONDS_STATE

        val intent = Intent(context, TimerService::class.java)
            .putExtra(TimerService.START_TIME_KEY, totalSeconds * 1000)
        runCatching {
            context.stopService(intent)
        }
        runCatching {
            context.unbindService(serviceConnection)
        }
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        currentTimeMillis = 0
    }

    fun tryConnect(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
    }

    fun pauseTimer() {
        service?.pause()
    }

    fun resumeTimer() {
        service?.resume()
    }

    fun stopTimer(context: Context) {
        service?.stop()
        context.unbindService(serviceConnection)
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
        timePickerSecondsState = timePickerSecondsState.substring(0, 4) +
                seconds.toString().padStart(2, '0')
    }

    fun addMinutes(minutes: Int) {
        timePickerSecondsState = timePickerSecondsState.substring(0, 2) +
            minutes.toString().padStart(2, '0') +
            timePickerSecondsState.substring(4)
    }

    fun addHours(hours: Int) {
        timePickerSecondsState = hours.toString().padStart(2, '0') +
            timePickerSecondsState.substring(2)
    }

    fun deleteLastNumber() {
        timePickerSecondsState = timePickerSecondsState.dropLast(1).ifEmpty { "0" }
    }

    fun clear() {
        timePickerSecondsState = INITIAL_SECONDS_STATE
    }
}
