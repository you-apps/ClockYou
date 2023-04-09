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
import com.bnyro.clock.services.StopwatchService

class StopwatchModel : ViewModel() {
    var state by mutableStateOf(WatchState.IDLE)
    var currentTimeMillis by mutableStateOf(0)

    @SuppressLint("StaticFieldLeak")
    private var service: StopwatchService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName, binder: IBinder) {
            service = (binder as ScheduleService.LocalBinder).getService() as? StopwatchService
            service?.changeListener = { state, time ->
                this@StopwatchModel.state = state
                this@StopwatchModel.currentTimeMillis = time
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            state = WatchState.IDLE
            service = null
        }
    }

    fun startStopwatch(context: Context) {
        val intent = Intent(context, StopwatchService::class.java)
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
        val intent = Intent(context, StopwatchService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
    }

    fun pauseStopwatch() {
        service?.pause()
    }

    fun resumeStopwatch() {
        service?.resume()
    }

    fun stopStopwatch(context: Context) {
        service?.stop()
        context.unbindService(serviceConnection)
    }
}
