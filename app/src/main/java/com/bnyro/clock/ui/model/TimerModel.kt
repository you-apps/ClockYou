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

class TimerModel : ViewModel() {
    var state by mutableStateOf(WatchState.IDLE)
    var currentTimeMillis by mutableStateOf(0)
    val hourPickerState = mutableStateOf(0)
    val minutePickerState = mutableStateOf(10)
    val secondPickerState = mutableStateOf(0)

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

    fun startTimer(context: Context) {
        val timerDelay = hourPickerState.value * 3600 + minutePickerState.value * 60 + secondPickerState.value
        if (timerDelay == 0) return

        val intent = Intent(context, TimerService::class.java)
            .putExtra(TimerService.START_TIME_KEY, timerDelay * 1000)
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
}
